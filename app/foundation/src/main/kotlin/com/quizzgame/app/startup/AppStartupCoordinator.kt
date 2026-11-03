package com.quizzgame.app.startup

import com.quizzgame.app.graph.AppGraph
import com.quizzgame.app.network.AppNetworkConfigurationException
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.core.error.AppFailure
import com.quizzgame.core.error.ConnectivityKind
import com.quizzgame.core.error.DefaultErrorClassifier
import com.quizzgame.core.error.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorOperationContext
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.OperationKind
import com.quizzgame.core.error.RetrySafety
import com.quizzgame.core.network.createNetworkJson
import com.quizzgame.core.session.SessionManager
import com.quizzgame.core.session.SessionState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class AppStartupCoordinator(
    private val openSession: suspend () -> SessionManager,
    private val networkFactory: AppNetworkGraphFactory,
    dispatcher: CoroutineDispatcher,
) : AppStartup {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val lock = Mutex()
    private val mutableState = MutableStateFlow<AppStartupState>(AppStartupState.Idle)
    override val state = mutableState.asStateFlow()
    private val mutableSessionState = MutableStateFlow<SessionState>(SessionState.Initializing)
    override val sessionState = mutableSessionState.asStateFlow()
    private var flight: Deferred<Unit>? = null
    private var session: SessionManager? = null
    private var appGraph: AppGraph? = null
    private var closed = false

    override suspend fun ensureStarted() = start(retry = false)
    override suspend fun retryInitialization() = start(retry = true)

    override suspend fun awaitGraph(): AppGraph {
        ensureStarted()
        return lock.withLock { checkNotNull(appGraph) { "Application graph is not ready" } }
    }

    private suspend fun start(retry: Boolean) {
        val task = lock.withLock {
            check(!closed) { "Application startup is closed" }
            if (appGraph != null) return
            val existing = flight
            if (existing != null && (!existing.isCompleted || !retry)) return@withLock existing
            scope.async { initialize() }.also { flight = it }
        }
        task.await()
    }

    private suspend fun initialize() {
        mutableState.value = AppStartupState.Loading
        try {
            val manager = session?.also { it.restoreStoredSession() } ?: openSession().also { session = it }
            val sessionState = manager.state.value
            mutableSessionState.value = sessionState
            if (sessionState is SessionState.Unavailable) {
                val failure = DefaultErrorClassifier().fromSession(sessionState)
                // Retrying a local read is safe; retain the sole owner of this DataStore file.
                mutableState.value = startupFailureState(failure, storageRetry = true)
                return
            }
            check(sessionState is SessionState.Ready)
            val graph = networkFactory.open(manager, createNetworkJson())
            var published = false
            try {
                lock.withLock {
                    if (!closed) {
                        appGraph = AppGraph(manager, graph)
                        published = true
                        mutableState.value = AppStartupState.Ready
                    }
                }
            } finally {
                if (!published) graph.close()
            }
            if (published) scope.launch { manager.state.collect { mutableSessionState.value = it } }
        } catch (_: AppNetworkConfigurationException) {
            mutableState.value = AppStartupState.Failed(
                ErrorPresentationSpec(ErrorSurface.BLOCKING, ErrorCopyKey.GENERIC_FAILURE),
                configurationRequired = true,
            )
        } catch (_: IOException) {
            mutableState.value = startupFailureState(AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE))
        }
    }

    suspend fun shutdown() {
        val pending = lock.withLock {
            if (closed) return
            closed = true
            flight?.also { it.cancel() }
        }
        pending?.join()
        scope.cancel()
        appGraph?.close() ?: session?.close()
        appGraph = null
        session = null
    }
}

internal fun startupFailureState(failure: AppFailure, storageRetry: Boolean = false): AppStartupState.Failed {
    val spec = DefaultErrorDecisionPolicy().decide(
        failure,
        ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT),
    )
    return AppStartupState.Failed(if (storageRetry) spec.copy(actions = listOf(ErrorActionKind.RETRY)) else spec)
}
