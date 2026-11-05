package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.storage.SessionIntent
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageFailure
import java.io.IOException
import kotlin.concurrent.withLock

internal class SessionStorageRecovery(private val runtime: SessionManager) {
    private val persistence = SessionMutations(runtime)

    fun restore(): SessionOperation = runtime.lock.withLock {
        val current = runtime.access.get()
        when {
            current == CredentialAccess.Closed -> SessionOperation.Closed
            !allowsRead(current) -> SessionOperation.Busy
            else -> readStored()
        }
    }

    fun recover(): SessionRecoveryResult = runtime.lock.withLock {
        when {
            runtime.access.get() == CredentialAccess.Closed -> SessionRecoveryResult.Closed
            runtime.recoveryAction.value == SessionRecoveryAction.REAUTHENTICATE ->
                SessionRecoveryResult.ReauthenticationRequired
            runtime.recoveryAction.value == SessionRecoveryAction.RETRY_READ -> readStored().asRecovery()
            runtime.recoveryAction.value == SessionRecoveryAction.RETRY_PERSISTENCE ->
                persistence.reconcile(runtime.pending as PendingSessionMutation.Write).asRecovery()
            else -> SessionRecoveryResult.NotAllowed
        }
    }

    private fun allowsRead(current: CredentialAccess): Boolean = when (current) {
        is CredentialAccess.Ready -> runtime.pending == null
        is CredentialAccess.Blocked ->
            current.kind == CredentialUnavailableKind.NOT_INITIALIZED ||
                runtime.recoveryAction.value == SessionRecoveryAction.RETRY_READ
        CredentialAccess.Closed -> false
    }

    private fun readStored(): SessionOperation {
        val previous = runtime.access.get()
        runtime.pending = PendingSessionMutation.Read
        if (previous.record != null) {
            runtime.block(CredentialUnavailableKind.STORAGE_UNAVAILABLE, previous.record)
        }
        return try {
            val record = runtime.store.read()
            when (record.intent) {
                SessionIntent.NONE -> {
                    runtime.publish(record)
                    SessionOperation.Applied
                }
                SessionIntent.REFRESH -> {
                    runtime.pending = PendingSessionMutation.UncertainRefresh
                    runtime.block(CredentialUnavailableKind.REFRESH_UNCERTAIN, record, record.epoch)
                    SessionOperation.Busy
                }
                SessionIntent.LOGOUT, SessionIntent.REVOKED -> completeTermination(record)
            }
        } catch (failure: IOException) {
            runtime.unavailable(failure, SessionStorageFailure.READ)
        }
    }

    private fun completeTermination(record: SessionRecord): SessionOperation {
        val kind = if (record.intent == SessionIntent.REVOKED) {
            SessionMutationKind.REVOKED
        } else {
            SessionMutationKind.LOGOUT
        }
        val target = record.updated(
            tokens = null,
            revision = Math.incrementExact(record.revision),
        )
        runtime.block(CredentialUnavailableKind.TERMINATING, record, record.epoch)
        return persistence.persist(PendingSessionMutation.Write(kind, record, target))
    }
}

private fun SessionOperation.asRecovery(): SessionRecoveryResult = when (this) {
    SessionOperation.Applied -> SessionRecoveryResult.Recovered
    SessionOperation.Busy, SessionOperation.Stale -> SessionRecoveryResult.NotAllowed
    SessionOperation.Closed -> SessionRecoveryResult.Closed
    is SessionOperation.Failed -> SessionRecoveryResult.NeedsStorageRecovery(reason)
}
