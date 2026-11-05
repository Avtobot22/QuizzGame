package com.quizzgame.app.startup

import com.quizzgame.app.graph.AppGraph
import com.quizzgame.app.network.AppNetworkConfigurationException
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionOperation
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

class AppStartupCoordinator(
    private val openSession: suspend () -> SessionManager,
    private val networkFactory: AppNetworkGraphFactory,
    dispatcher: CoroutineDispatcher,
    private val recovery: StorageRecoveryPolicy = StorageRecoveryPolicy(),
) : AppStartup {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val lock = Mutex()
    private val mutableState = MutableStateFlow<AppStartupState>(AppStartupState.Idle)
    override val state = mutableState.asStateFlow()
    private val mutableSessionState = MutableStateFlow<SessionState>(SessionState.Initializing)
    override val sessionState = mutableSessionState.asStateFlow()
    private var flight: Deferred<Unit>? = null
    private var logoutFlight: Deferred<SessionOperation>? = null
    private var session: SessionManager? = null
    private var appGraph: AppGraph? = null
    private var closed = false
    private var shutdownCompletion: CompletableDeferred<Unit>? = null

    override suspend fun ensureStarted() = start(retry = false)
    override suspend fun retryInitialization() = start(retry = true)

    override suspend fun logout(): SessionOperation {
        ensureStarted()
        val task = lock.withLock {
            val manager = session
            val current = manager?.state?.value
            val blocked = when {
                closed || current == SessionState.Closed -> SessionOperation.Closed
                current is SessionState.Unavailable -> SessionOperation.Failed(current.reason)
                manager == null -> SessionOperation.Failed(
                    ((mutableState.value as? AppStartupState.Failed)?.reason as? AppStartupFailure.Storage)?.reason
                        ?: error("Startup completed without a session or storage failure"),
                )
                else -> null
            }
            if (blocked != null) {
                CompletableDeferred(blocked)
            } else {
                logoutFlight?.takeUnless { it.isCompleted }
                    ?: scope.async { requireNotNull(manager).logout() }.also { logoutFlight = it }
            }
        }
        return task.await()
    }

    override suspend fun accessGraph(): GraphAccessResult {
        try {
            ensureStarted()
        } catch (cancelled: CancellationException) {
            currentCoroutineContext().ensureActive()
            if (!lock.withLock { closed }) throw cancelled
        }
        return lock.withLock {
            val current = session?.state?.value
            when {
                closed || current == SessionState.Closed -> GraphAccessResult.Closed
                appGraph != null -> GraphAccessResult.Ready(requireNotNull(appGraph))
                current is SessionState.Unavailable -> GraphAccessResult.Unavailable(
                    startupFailureState(AppStartupFailure.Storage(current.reason), recovery),
                )
                current is SessionState.Ready && appGraph != null -> GraphAccessResult.Ready(requireNotNull(appGraph))
                mutableState.value is AppStartupState.Failed ->
                    GraphAccessResult.Unavailable(mutableState.value as AppStartupState.Failed)
                else -> GraphAccessResult.Ready(
                    checkNotNull(appGraph) { "Startup completed without a graph or failure" },
                )
            }
        }
    }

    private suspend fun start(retry: Boolean) {
        val task = lock.withLock {
            val current = session?.state?.value
            val existing = flight
            val failure = mutableState.value as? AppStartupState.Failed
            when {
                closed || (appGraph != null && current is SessionState.Ready) -> null
                existing != null && (!existing.isCompleted || !retry) -> existing
                retry && failure != null && ErrorActionKind.RETRY !in failure.error.actions -> null
                retry && !recovery.allowsRetry(current) -> null
                else -> scope.async { initialize(retry) }.also { flight = it }
            }
        }
        task?.await()
    }

    private suspend fun initialize(retry: Boolean) {
        lock.withLock { if (!closed) mutableState.value = AppStartupState.Loading }
        val manager = prepareSession(retry) ?: return
        if (lock.withLock { appGraph != null }) {
            lock.withLock { if (!closed) mutableState.value = manager.state.value.startupState(recovery) }
            return
        }
        try {
            val graph = networkFactory.open(manager, createNetworkJson())
            var retained = false
            try {
                lock.withLock {
                    if (!closed) {
                        appGraph = AppGraph(manager, graph)
                        retained = true
                        mutableState.value = manager.state.value.startupState(recovery)
                    }
                }
            } finally {
                if (!retained) graph.close()
            }
        } catch (_: AppNetworkConfigurationException) {
            lock.withLock {
                if (!closed) mutableState.value = startupFailureState(AppStartupFailure.NetworkConfiguration, recovery)
            }
        } catch (_: IOException) {
            lock.withLock {
                if (!closed) mutableState.value = startupFailureState(AppStartupFailure.Transport, recovery)
            }
        }
    }

    private suspend fun prepareSession(retry: Boolean): SessionManager? {
        val retained = lock.withLock { session }
        val manager = try {
            retained?.also { if (retry && it.state.value is SessionState.Unavailable) it.recoverStorage() }
                ?: acquireSession()
        } catch (failure: IOException) {
            val reason = (failure as? SessionStorageException)?.failure ?: SessionStorageFailure.READ
            lock.withLock {
                if (!closed) mutableState.value = startupFailureState(AppStartupFailure.Storage(reason), recovery)
            }
            return null
        }
        val current = manager.state.value
        return lock.withLock {
            if (closed) return@withLock null
            mutableSessionState.value = current
            when (current) {
                is SessionState.Unavailable -> {
                    mutableState.value = startupFailureState(AppStartupFailure.Storage(current.reason), recovery)
                    null
                }
                SessionState.Closed -> {
                    mutableState.value = AppStartupState.Closed
                    null
                }
                is SessionState.Ready, is SessionState.Blocked -> manager
                SessionState.Initializing -> error("Session factory returned an uninitialized manager")
            }
        }
    }

    private suspend fun acquireSession(): SessionManager {
        val acquired = openSession()
        var owned = false
        try {
            lock.withLock {
                if (!closed) {
                    session = acquired
                    owned = true
                    observeSession(acquired)
                }
            }
        } finally {
            if (!owned) acquired.close()
        }
        return acquired
    }

    private fun observeSession(manager: SessionManager) {
        scope.launch {
            manager.state.collect {
                lock.withLock {
                    if (!closed) {
                        val current = manager.state.value
                        mutableSessionState.value = current
                        if (appGraph != null || current !is SessionState.Ready) {
                            mutableState.value = current.startupState(recovery)
                        }
                    }
                }
            }
        }
    }

    suspend fun shutdown() {
        val work = lock.withLock {
            val existing = shutdownCompletion
            if (existing != null) return@withLock ShutdownWork(existing, null, null, false)
            closed = true
            mutableState.value = AppStartupState.Closed
            mutableSessionState.value = SessionState.Closed
            val completion = CompletableDeferred<Unit>().also { shutdownCompletion = it }
            ShutdownWork(completion, flight, logoutFlight, true)
        }
        if (!work.ownsCleanup) return work.completion.await()
        withContext(NonCancellable) {
            val result = runCatching {
                work.pending?.cancelAndJoin()
                work.pendingLogout?.cancelAndJoin()
                scope.cancel()
                val resources = lock.withLock {
                    (appGraph to session).also {
                        appGraph = null
                        session = null
                    }
                }
                resources.first?.close() ?: resources.second?.close()
                Unit
            }
            work.completion.completeWith(result)
            result.getOrThrow()
        }
    }
}

private data class ShutdownWork(
    val completion: CompletableDeferred<Unit>,
    val pending: Deferred<Unit>?,
    val pendingLogout: Deferred<SessionOperation>?,
    val ownsCleanup: Boolean,
)
