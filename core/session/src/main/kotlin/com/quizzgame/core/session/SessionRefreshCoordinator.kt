package com.quizzgame.core.session

import com.quizzgame.core.network.CredentialsSnapshot
import com.quizzgame.core.network.RefreshAttempt
import com.quizzgame.core.network.RefreshCoordinator
import com.quizzgame.core.network.RefreshFailureReason
import com.quizzgame.core.network.RefreshOutcome
import com.quizzgame.core.session.api.RefreshCall
import com.quizzgame.core.session.api.RefreshClient
import com.quizzgame.core.session.api.RefreshException
import com.quizzgame.core.session.api.RefreshFailure
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

    override fun register(observed: CredentialsSnapshot): RefreshAttempt = lock.withLock {
        val cohort = current?.takeIf { !it.completed && it.observed.matches(observed) }
            ?: Cohort(observed).also { current = it }
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
        val state = manager.refreshState
        if (state == null || !state.credentials.matches(cohort.observed)) {
            cohort.outcome = resolve(cohort) ?: RefreshOutcome.SessionUnavailable
            cohort.completed = true
            return
        }
        cohort.call = refreshClient.newCall(state.tokens.refresh, state.deviceId)
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
            outcome = execute(cohort)
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

    private fun execute(cohort: Cohort): RefreshOutcome {
        val early = lock.withLock {
            if (closed || cohort.users == 0) RefreshOutcome.Cancelled else resolve(cohort)
        }
        if (early != null) return early
        return try {
            val tokens = checkNotNull(cohort.call).execute()
            commit(cohort) { manager.applyRefreshedTokensIfCurrentBlocking(cohort.observed, tokens) }
        } catch (failure: RefreshException) {
            if (revocationPolicy.isRevoked(failure.failure)) {
                commit(cohort, revoked = true) { manager.expireIfCurrentBlocking(cohort.observed) }
            } else {
                RefreshOutcome.Failed(failure.failure.toReason())
            }
        } catch (_: CancellationException) {
            RefreshOutcome.Cancelled
        }
    }

    private fun commit(
        cohort: Cohort,
        revoked: Boolean = false,
        operation: () -> SessionOperation,
    ): RefreshOutcome {
        lock.withLock {
            if (closed || cohort.users == 0) return RefreshOutcome.Cancelled
        }
        return when (operation()) {
            SessionOperation.Applied                        -> if (revoked) {
                RefreshOutcome.Revoked
            } else {
                manager.refreshState?.credentials?.let(RefreshOutcome::Ready) ?: RefreshOutcome.SessionUnavailable
            }

            is SessionOperation.Failed                      -> RefreshOutcome.Failed(RefreshFailureReason.Storage)
            SessionOperation.Stale, SessionOperation.Closed -> RefreshOutcome.SessionUnavailable
        }
    }

    private fun resolve(cohort: Cohort): RefreshOutcome? {
        if (closed) return RefreshOutcome.SessionUnavailable
        val state = manager.refreshState ?: return cohort.outcome?.takeIf { it == RefreshOutcome.Revoked }
            ?: RefreshOutcome.SessionUnavailable
        if (state.credentials.sessionGeneration != cohort.observed.sessionGeneration) {
            return RefreshOutcome.SessionUnavailable
        }
        if (!state.credentials.matches(cohort.observed)) return RefreshOutcome.Ready(state.credentials)
        return cohort.outcome
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

private fun CredentialsSnapshot.matches(other: CredentialsSnapshot): Boolean =
    sessionGeneration == other.sessionGeneration && credentialsRevision == other.credentialsRevision &&
        accessToken == other.accessToken

private fun RefreshFailure.toReason(): RefreshFailureReason = when (this) {
    is RefreshFailure.Http         -> RefreshFailureReason.Http(status, backend)
    is RefreshFailure.Diagnostic   -> RefreshFailureReason.Diagnostic(failure)
    RefreshFailure.Io              -> RefreshFailureReason.Io
    RefreshFailure.Timeout         -> RefreshFailureReason.Timeout
    RefreshFailure.InvalidResponse -> RefreshFailureReason.InvalidResponse
}
