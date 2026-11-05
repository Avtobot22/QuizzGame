package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.auth.CredentialsProvider
import com.quizzgame.core.network.auth.CredentialsSnapshot
import com.quizzgame.core.session.model.DeviceIdProvider
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.core.session.storage.SessionStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Suppress("TooManyFunctions")
class SessionManager private constructor(
    internal val store: SessionStore,
    internal val dispatcher: CoroutineDispatcher,
) : CredentialsProvider, SessionActions, Closeable {
    internal val lock = ReentrantLock()
    internal val access = AtomicReference<CredentialAccess>(
        CredentialAccess.Blocked(CredentialUnavailableKind.NOT_INITIALIZED, null, 0, null),
    )
    internal var pending: PendingSessionMutation? = PendingSessionMutation.Read
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Initializing)
    override val state = mutableState.asStateFlow()
    private val mutableRecoveryAction = MutableStateFlow(SessionRecoveryAction.NONE)
    override val recoveryAction = mutableRecoveryAction.asStateFlow()
    val deviceIdProvider = DeviceIdProvider {
        access.get().record?.deviceId ?: throw SessionUnavailableException(
            if (access.get() == CredentialAccess.Closed) {
                CredentialUnavailableKind.CLOSED
            } else {
                CredentialUnavailableKind.NOT_INITIALIZED
            },
        )
    }

    override fun snapshot(): CredentialsSnapshot = admitted().snapshot()

    private fun admitted(): CredentialAccess.Ready = when (val current = access.get()) {
        is CredentialAccess.Ready -> current
        is CredentialAccess.Blocked -> throw SessionUnavailableException(current.kind)
        CredentialAccess.Closed -> throw SessionUnavailableException(CredentialUnavailableKind.CLOSED)
    }

    internal fun publish(record: SessionRecord, expired: Boolean = false) {
        val ready = CredentialAccess.Ready(record, expired)
        pending = null
        access.set(ready)
        mutableState.value = ready.state
        mutableRecoveryAction.value = SessionRecoveryAction.NONE
    }

    internal fun block(kind: CredentialUnavailableKind, record: SessionRecord?, epoch: Long = access.get().epoch) {
        val previous = access.get()
        access.set(CredentialAccess.Blocked(kind, record, epoch, previous.lastKnown))
        mutableState.value = SessionState.Blocked(kind, previous.lastKnown)
        mutableRecoveryAction.value = if (pending == PendingSessionMutation.UncertainRefresh) {
            SessionRecoveryAction.REAUTHENTICATE
        } else {
            SessionRecoveryAction.NONE
        }
    }

    internal fun unavailable(failure: IOException, fallback: SessionStorageFailure): SessionOperation.Failed {
        val reason = (failure as? SessionStorageException)?.failure ?: fallback
        val previous = access.get()
        access.set(
            CredentialAccess.Blocked(
                CredentialUnavailableKind.STORAGE_UNAVAILABLE,
                previous.record,
                previous.epoch,
                previous.lastKnown,
                reason,
            ),
        )
        mutableState.value = SessionState.Unavailable(reason, previous.lastKnown)
        mutableRecoveryAction.value = recoveryFor(reason)
        return SessionOperation.Failed(reason)
    }

    private fun recoveryFor(reason: SessionStorageFailure): SessionRecoveryAction = when {
        reason == SessionStorageFailure.CRYPTO || reason == SessionStorageFailure.CORRUPT -> SessionRecoveryAction.NONE
        pending is PendingSessionMutation.Write -> SessionRecoveryAction.RETRY_PERSISTENCE
        pending == PendingSessionMutation.Read -> SessionRecoveryAction.RETRY_READ
        else -> SessionRecoveryAction.NONE
    }

    private val mutations = SessionMutations(this)
    private val recovery = SessionStorageRecovery(this)
    private val refresh = SessionRefreshTransactions(this)

    internal val refreshState: SessionRefreshState? get() =
        (access.get() as? CredentialAccess.Ready)?.record?.let { record ->
            record.tokens?.let {
                SessionRefreshState(CredentialsSnapshot(it.access, record.epoch, record.revision), record.deviceId, it)
            }
        }

    override suspend fun signIn(tokens: TokenPair): SessionChangeResult = startSession(tokens).asChange()
    override suspend fun signOut(): SessionChangeResult = logout().asChange()
    override suspend fun recoverStorage(): SessionRecoveryResult = withContext(dispatcher) { recovery.recover() }
    suspend fun startSession(tokens: TokenPair): SessionOperation = withContext(dispatcher) { mutations.signIn(tokens) }
    suspend fun logout(): SessionOperation = withContext(dispatcher) { mutations.signOut() }
    suspend fun restoreStoredSession(): SessionOperation = withContext(dispatcher) { recovery.restore() }

    fun beginRefreshBlocking(observed: CredentialsSnapshot): SessionOperation = refresh.begin(observed)
    fun finishRefreshTemporaryBlocking(observed: CredentialsSnapshot): SessionOperation = refresh.temporary(observed)
    fun markRefreshUncertainIfCurrentBlocking(observed: CredentialsSnapshot): SessionOperation = refresh.uncertain(
        observed,
    )
    fun applyRefreshedTokensIfCurrentBlocking(observed: CredentialsSnapshot, tokens: TokenPair): SessionOperation =
        refresh.apply(observed, tokens)
    fun expireIfCurrentBlocking(observed: CredentialsSnapshot): SessionOperation = mutations.expire(observed)

    override fun close() = lock.withLock {
        if (access.get() != CredentialAccess.Closed) {
            access.set(CredentialAccess.Closed)
            pending = null
            mutableState.value = SessionState.Closed
            mutableRecoveryAction.value = SessionRecoveryAction.NONE
            store.close()
        }
    }

    companion object {
        suspend fun open(
            sessionStore: SessionStore,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): SessionManager {
            val manager = SessionManager(sessionStore, ioDispatcher)
            var acquired = false
            try {
                manager.restoreStoredSession()
                acquired = true
                return manager
            } finally {
                if (!acquired) manager.close()
            }
        }
    }
}

internal class SessionRefreshState(
    val credentials: CredentialsSnapshot,
    val deviceId: String,
    val tokens: TokenPair,
) {
    override fun toString(): String = "SessionRefreshState(<redacted>)"
}

private fun SessionOperation.asChange(): SessionChangeResult = when (this) {
    SessionOperation.Applied -> SessionChangeResult.Committed
    SessionOperation.Busy, SessionOperation.Stale -> SessionChangeResult.BusyOrBlocked
    SessionOperation.Closed -> SessionChangeResult.Closed
    is SessionOperation.Failed -> SessionChangeResult.NeedsStorageRecovery(reason)
}
