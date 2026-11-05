package com.quizzgame.core.session.refresh

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.auth.CredentialsSnapshot
import com.quizzgame.core.network.auth.RefreshAttempt
import com.quizzgame.core.network.auth.RefreshCoordinator
import com.quizzgame.core.network.auth.RefreshFailureReason
import com.quizzgame.core.network.auth.RefreshOutcome
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionOperation
import com.quizzgame.core.session.lifecycle.SessionRefreshState
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.refresh.api.RefreshCall
import com.quizzgame.core.session.refresh.api.RefreshClient
import com.quizzgame.core.session.refresh.api.RefreshException
import com.quizzgame.core.session.refresh.api.RefreshFailure
import com.quizzgame.core.session.refresh.api.isKnownRejected
import com.quizzgame.core.session.refresh.api.toRefreshFailureReason
import java.io.Closeable
import java.util.concurrent.CancellationException
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SessionRefreshCoordinator(
    private val manager: SessionManager,
    private val refreshClient: RefreshClient,
    private val refreshExecutor: Executor,
    private val revocationPolicy: RefreshRevocationPolicy = RefreshRevocationPolicy.never(),
) : RefreshCoordinator, Closeable {

    private class Cohort(val observed: CredentialsSnapshot) {

        var users = 0
        var running = false
        var completed = false
        var call: RefreshCall? = null
        var outcome: RefreshOutcome? = null
    }

    private inner class Lease(val cohort: Cohort) : RefreshAttempt {

        var cancelled = false
        var released = false
        override fun refresh(): RefreshOutcome = lock.withLock {
            var result: RefreshOutcome? = null
            while (result == null) {
                result = when {
                    cancelled          -> RefreshOutcome.Cancelled
                    closed || released -> RefreshOutcome.SessionUnavailable
                    else               -> resolve(cohort)
                }
                if (result == null) {
                    if (!cohort.running) startFlight(cohort)
                    if (!cohort.completed) {
                        try {
                            changed.await()
                        } catch (_: InterruptedException) {
                            cancel()
                            Thread.currentThread().interrupt()
                            result = RefreshOutcome.Cancelled
                        }
                    }
                }
            }
            result
        }

        override fun cancel() = release(this, cancel = true)
        override fun close() = release(this, cancel = false)
    }

    private val lock = ReentrantLock()
    private val changed = lock.newCondition()
    private val active = mutableSetOf<Cohort>()
    private var current: Cohort? = null
    private var closed = false
    private val execution = RefreshExecution(manager, refreshClient, revocationPolicy)

    override fun register(observed: CredentialsSnapshot): RefreshAttempt = lock.withLock {
        val cohort = current?.takeIf {
            !it.completed && it.observed.matchesRefresh(observed)
        } ?: Cohort(observed).also { current = it }
        cohort.users++
        active += cohort
        Lease(cohort)
    }

    override fun close() = lock.withLock {
        closed = true
        active.forEach { it.call?.cancel() }
        changed.signalAll()
    }

    private fun startFlight(cohort: Cohort) {
        cohort.running = true
        try {
            refreshExecutor.execute { runFlight(cohort) }
        } catch (_: RejectedExecutionException) {
            cohort.call?.cancel()
            cohort.outcome = RefreshOutcome.SessionUnavailable
            cohort.completed = true
            cohort.running = false
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun runFlight(cohort: Cohort) {
        var outcome: RefreshOutcome = RefreshOutcome.Failed(RefreshFailureReason.Io)
        try {
            outcome = execution.run(cohort.observed) { call ->
                lock.withLock {
                    if (closed || cohort.users == 0) {
                        false
                    } else {
                        cohort.call = call
                        true
                    }
                }
            }
        } catch (failure: Exception) {
            outcome = RefreshOutcome.Unexpected(failure)
        } finally {
            lock.withLock {
                cohort.outcome = outcome
                cohort.completed = true
                cohort.running = false
                cohort.call = null
                if (cohort.users == 0) retire(cohort)
                changed.signalAll()
            }
        }
    }

    private fun resolve(cohort: Cohort): RefreshOutcome? {
        val state = manager.refreshState
        return when {
            closed -> RefreshOutcome.SessionUnavailable
            state != null && state.credentials.sessionGeneration != cohort.observed.sessionGeneration ->
                RefreshOutcome.SessionUnavailable
            state != null && !state.credentials.matchesRefresh(cohort.observed) -> RefreshOutcome.Ready(
                state.credentials,
            )
            cohort.outcome != null -> cohort.outcome
            // Our flight can temporarily close admission while B is being committed.
            cohort.running || state != null -> null
            else -> currentUnavailable(manager)
        }
    }

    private fun release(lease: Lease, cancel: Boolean) = lock.withLock {
        if (cancel) lease.cancelled = true
        if (!lease.released) {
            lease.released = true
            val cohort = lease.cohort
            cohort.users--
            if (cohort.users == 0) {
                cohort.call?.cancel()
                if (!cohort.running) retire(cohort)
            }
        }
        changed.signalAll()
    }

    private fun retire(cohort: Cohort) {
        active -= cohort
        if (current === cohort) current = null
    }
}

/** Runs durable transitions and HTTP outside the coordinator's waiter lock. */
internal class RefreshExecution(
    private val manager: SessionManager,
    private val client: RefreshClient,
    private val revocation: RefreshRevocationPolicy,
) {
    fun run(
        observed: CredentialsSnapshot,
        attach: (RefreshCall) -> Boolean,
    ): RefreshOutcome {
        val prepared = manager.beginRefreshBlocking(observed)
        val state = manager.refreshState
        return when {
            prepared != SessionOperation.Applied -> prepared.unavailableOutcome()
            state == null || !state.credentials.matchesRefresh(observed) -> currentUnavailable(manager)
            else -> runPrepared(observed, state, attach)
        }
    }

    private fun runPrepared(
        observed: CredentialsSnapshot,
        state: SessionRefreshState,
        attach: (RefreshCall) -> Boolean,
    ): RefreshOutcome {
        var finished = false
        try {
            val call = client.newCall(state.tokens.refresh, state.deviceId)
            val result = if (attach(call)) {
                execute(call, observed)
            } else {
                call.cancel()
                cancel(observed, call)
            }
            finished = true
            return result
        } finally {
            if (!finished) manager.markRefreshUncertainIfCurrentBlocking(observed)
        }
    }

    private fun execute(call: RefreshCall, observed: CredentialsSnapshot): RefreshOutcome = try {
        commit(manager.applyRefreshedTokensIfCurrentBlocking(observed, call.execute()))
    } catch (failure: RefreshException) {
        failed(observed, failure.failure)
    } catch (_: CancellationException) {
        cancel(observed, call)
    }

    private fun failed(observed: CredentialsSnapshot, failure: RefreshFailure): RefreshOutcome {
        val reason = failure.toRefreshFailureReason()
        return when {
            revocation.isRevoked(failure) -> commit(manager.expireIfCurrentBlocking(observed), revoked = true)
            failure.isKnownRejected() -> {
                val restored = manager.finishRefreshTemporaryBlocking(observed)
                if (restored == SessionOperation.Applied) RefreshOutcome.Failed(reason) else commit(restored)
            }
            else -> {
                manager.markRefreshUncertainIfCurrentBlocking(observed)
                RefreshOutcome.UnknownOutcome(reason)
            }
        }
    }

    private fun cancel(observed: CredentialsSnapshot, call: RefreshCall): RefreshOutcome {
        if (call.hasExecuted) {
            manager.markRefreshUncertainIfCurrentBlocking(observed)
        } else {
            manager.finishRefreshTemporaryBlocking(observed)
        }
        return RefreshOutcome.Cancelled
    }

    private fun commit(operation: SessionOperation, revoked: Boolean = false): RefreshOutcome = when (operation) {
        SessionOperation.Applied -> if (revoked) {
            RefreshOutcome.Revoked
        } else {
            manager.refreshState?.credentials?.let(RefreshOutcome::Ready) ?: currentUnavailable(manager)
        }
        is SessionOperation.Failed -> RefreshOutcome.PersistencePending
        SessionOperation.Busy -> currentUnavailable(manager)
        SessionOperation.Stale, SessionOperation.Closed -> RefreshOutcome.SessionUnavailable
    }

    private fun SessionOperation.unavailableOutcome(): RefreshOutcome = when (this) {
        is SessionOperation.Failed -> RefreshOutcome.PersistencePending
        else -> currentUnavailable(manager)
    }
}

internal fun currentUnavailable(manager: SessionManager): RefreshOutcome = when (val state = manager.state.value) {
    is SessionState.Unavailable -> RefreshOutcome.CredentialsUnavailable(CredentialUnavailableKind.STORAGE_UNAVAILABLE)
    is SessionState.Blocked -> RefreshOutcome.CredentialsUnavailable(state.kind)
    SessionState.Closed -> RefreshOutcome.CredentialsUnavailable(CredentialUnavailableKind.CLOSED)
    else -> RefreshOutcome.SessionUnavailable
}

internal fun CredentialsSnapshot.matchesRefresh(other: CredentialsSnapshot): Boolean =
    sessionGeneration == other.sessionGeneration && credentialsRevision == other.credentialsRevision &&
        accessToken == other.accessToken
