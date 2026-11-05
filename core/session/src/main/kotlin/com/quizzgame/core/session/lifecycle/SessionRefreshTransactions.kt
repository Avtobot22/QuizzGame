package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.auth.CredentialsSnapshot
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionIntent
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import java.io.IOException
import kotlin.concurrent.withLock

internal class SessionRefreshTransactions(private val runtime: SessionManager) {
    private val persistence = SessionMutations(runtime)

    fun begin(observed: CredentialsSnapshot): SessionOperation = runtime.lock.withLock {
        val current = runtime.access.get()
        when {
            current == CredentialAccess.Closed -> SessionOperation.Closed
            current !is CredentialAccess.Ready || !current.matches(observed) -> SessionOperation.Stale
            runtime.pending != null -> SessionOperation.Busy
            else -> persistIntent(current, observed)
        }
    }

    fun apply(observed: CredentialsSnapshot, tokens: TokenPair): SessionOperation = runtime.lock.withLock {
        val current = runtime.access.get()
        val record = current.matchingRecord(observed, allowRecovery = true)
        when {
            current == CredentialAccess.Closed -> SessionOperation.Closed
            record == null -> SessionOperation.Stale
            !ownsRefresh(observed) -> SessionOperation.Busy
            else -> {
                val target = record.updated(tokens = tokens, revision = Math.incrementExact(record.revision))
                val mutation = PendingSessionMutation.Write(SessionMutationKind.REFRESH_PAIR, record, target)
                runtime.block(CredentialUnavailableKind.STORAGE_UNAVAILABLE, record)
                persistence.persist(mutation)
            }
        }
    }

    fun temporary(observed: CredentialsSnapshot): SessionOperation = runtime.lock.withLock {
        val current = runtime.access.get()
        when {
            current == CredentialAccess.Closed -> SessionOperation.Closed
            current !is CredentialAccess.Ready || !current.matches(observed) -> SessionOperation.Stale
            runtime.pending !is PendingSessionMutation.RefreshInFlight -> SessionOperation.Busy
            else -> {
                val target = current.record.updated()
                val mutation = PendingSessionMutation.Write(SessionMutationKind.RESTORE_REFRESH, current.record, target)
                runtime.block(CredentialUnavailableKind.STORAGE_UNAVAILABLE, current.record)
                persistence.persist(mutation)
            }
        }
    }

    fun uncertain(
        observed: CredentialsSnapshot,
    ): SessionOperation = runtime.lock.withLock {
        val current = runtime.access.get()
        val record = current.matchingRecord(observed, allowRecovery = true)
        when {
            current == CredentialAccess.Closed -> SessionOperation.Closed
            record == null -> SessionOperation.Stale
            runtime.pending == null -> {
                val started = persistIntent(current as CredentialAccess.Ready, observed)
                if (started == SessionOperation.Applied) {
                    blockUncertain(requireNotNull(runtime.access.get().record))
                } else {
                    started
                }
            }
            runtime.pending == PendingSessionMutation.UncertainRefresh -> blockUncertain(record)
            runtime.pending !is PendingSessionMutation.RefreshInFlight -> SessionOperation.Busy
            else -> blockUncertain(record)
        }
    }

    private fun blockUncertain(record: SessionRecord): SessionOperation {
        runtime.pending = PendingSessionMutation.UncertainRefresh
        runtime.block(CredentialUnavailableKind.REFRESH_UNCERTAIN, record)
        return SessionOperation.Applied
    }

    private fun persistIntent(current: CredentialAccess.Ready, observed: CredentialsSnapshot): SessionOperation {
        val marker = current.record.updated(
            intent = SessionIntent.REFRESH,
        )
        runtime.pending = PendingSessionMutation.Write(SessionMutationKind.RESTORE_REFRESH, marker, current.record)
        return try {
            if (!runtime.store.writeRecord(marker).sameContents(marker)) {
                throw SessionStorageException(SessionStorageFailure.CORRUPT)
            }
            runtime.access.set(CredentialAccess.Ready(marker))
            runtime.pending = PendingSessionMutation.RefreshInFlight(observed, marker)
            SessionOperation.Applied
        } catch (failure: IOException) {
            runtime.unavailable(failure, SessionStorageFailure.WRITE)
        }
    }

    private fun ownsRefresh(observed: CredentialsSnapshot): Boolean = when (val pending = runtime.pending) {
        null -> true
        is PendingSessionMutation.RefreshInFlight ->
            pending.observed.sessionGeneration == observed.sessionGeneration &&
                pending.observed.credentialsRevision == observed.credentialsRevision &&
                pending.observed.accessToken == observed.accessToken
        PendingSessionMutation.UncertainRefresh -> true
        else -> false
    }
}
