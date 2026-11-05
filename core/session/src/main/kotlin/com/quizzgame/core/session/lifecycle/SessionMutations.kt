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

internal class SessionMutations(private val runtime: SessionManager) {

    fun signIn(tokens: TokenPair): SessionOperation = runtime.lock.withLock {
        change(tokens, SessionMutationKind.SIGN_IN, SessionIntent.LOGOUT)
    }

    fun signOut(): SessionOperation = runtime.lock.withLock {
        change(null, SessionMutationKind.LOGOUT, SessionIntent.LOGOUT)
    }

    fun expire(observed: CredentialsSnapshot): SessionOperation = runtime.lock.withLock {
        val current = runtime.access.get()
        when {
            current == CredentialAccess.Closed -> SessionOperation.Closed
            current.matchingRecord(observed, allowRecovery = true) == null -> SessionOperation.Stale
            else -> change(null, SessionMutationKind.REVOKED, SessionIntent.REVOKED)
        }
    }

    private fun change(
        tokens: TokenPair?,
        kind: SessionMutationKind,
        intent: SessionIntent,
    ): SessionOperation {
        val current = runtime.access.get()
        val record = current.record
        return when {
            current == CredentialAccess.Closed -> SessionOperation.Closed
            current is CredentialAccess.Blocked && current.storageFailure != null ->
                SessionOperation.Failed(current.storageFailure)
            record == null || !allowsChange(current) -> SessionOperation.Busy
            else -> {
                val epoch = Math.incrementExact(current.epoch)
                val marker = record.updated(epoch = epoch, intent = intent)
                val target = record.updated(
                    tokens,
                    epoch,
                    Math.incrementExact(record.revision),
                )
                val mutation = PendingSessionMutation.Write(kind, record, target, marker)
                runtime.block(CredentialUnavailableKind.TERMINATING, record, epoch)
                persist(mutation)
            }
        }
    }

    private fun allowsChange(current: CredentialAccess): Boolean = when (current) {
        is CredentialAccess.Ready ->
            runtime.pending == null || runtime.pending is PendingSessionMutation.RefreshInFlight
        is CredentialAccess.Blocked ->
            runtime.pending == PendingSessionMutation.UncertainRefresh &&
                current.kind == CredentialUnavailableKind.REFRESH_UNCERTAIN
        CredentialAccess.Closed -> false
    }

    fun persist(mutation: PendingSessionMutation.Write, writeMarker: Boolean = true): SessionOperation {
        runtime.pending = mutation
        return try {
            if (writeMarker) mutation.marker?.let(::writeExpected)
            writeExpected(mutation.target)
            publish(mutation)
        } catch (failure: IOException) {
            runtime.unavailable(failure, SessionStorageFailure.WRITE)
        }
    }

    fun reconcile(mutation: PendingSessionMutation.Write): SessionOperation = try {
        val durable = runtime.store.read()
        when {
            durable.sameContents(mutation.target) -> publish(mutation)
            durable.sameContents(mutation.before) -> persist(mutation)
            mutation.marker?.sameContents(durable) == true -> persist(mutation, writeMarker = false)
            else -> runtime.unavailable(
                SessionStorageException(SessionStorageFailure.CORRUPT),
                SessionStorageFailure.READ,
            )
        }
    } catch (failure: IOException) {
        runtime.unavailable(failure, SessionStorageFailure.READ)
    }

    private fun publish(mutation: PendingSessionMutation.Write): SessionOperation {
        runtime.publish(mutation.target, expired = mutation.kind == SessionMutationKind.REVOKED)
        return SessionOperation.Applied
    }

    private fun writeExpected(record: SessionRecord) {
        if (!runtime.store.writeRecord(record).sameContents(record)) {
            throw SessionStorageException(SessionStorageFailure.CORRUPT)
        }
    }
}
