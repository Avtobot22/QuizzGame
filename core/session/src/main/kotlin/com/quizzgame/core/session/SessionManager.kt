package com.quizzgame.core.session

import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.network.CredentialsSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SessionManager private constructor(
    private val sessionStore: SessionStore,
    private val ioDispatcher: CoroutineDispatcher,
) : CredentialsProvider, Closeable {

    private class StoredSession(
        val record: SessionRecord,
        val generation: Long,
        val revision: Long,
        val state: SessionState.Ready,
    ) {

        fun matches(observed: CredentialsSnapshot): Boolean =
            record.tokens != null &&
                generation == observed.sessionGeneration &&
                revision == observed.credentialsRevision &&
                record.tokens.access == observed.accessToken
    }

    private enum class Change {
        LOGIN,
        LOGOUT,
        REFRESH,
        EXPIRE,
    }

    private val lock = ReentrantLock()
    private val ready = AtomicReference<StoredSession?>(null)

    val deviceIdProvider = DeviceIdProvider { ready.get()?.record?.deviceId ?: throw SessionUnavailableException() }

    private var closed = false
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Initializing)
    val state: StateFlow<SessionState> = mutableState.asStateFlow()

    override fun close() = lock.withLock {
        if (!closed) {
            closed = true
            ready.set(null)
            mutableState.value = SessionState.Closed
            sessionStore.close()
        }
    }

    override fun snapshot(): CredentialsSnapshot {
        val value = ready.get() ?: throw SessionUnavailableException()
        return CredentialsSnapshot(value.record.tokens?.access, value.generation, value.revision)
    }

    internal val refreshState: SessionRefreshState? get() = ready.get()?.let { value ->
        value.record.tokens?.let { tokens ->
            SessionRefreshState(
                CredentialsSnapshot(tokens.access, value.generation, value.revision),
                value.record.deviceId,
                tokens,
            )
        }
    }

    suspend fun startSession(tokens: TokenPair): SessionOperation = withContext(ioDispatcher) {
        save(tokens, Change.LOGIN)
    }

    suspend fun logout(): SessionOperation = withContext(ioDispatcher) { save(null, Change.LOGOUT) }

    suspend fun restoreStoredSession(): SessionOperation = withContext(ioDispatcher) {
        lock.withLock {
            if (closed) return@withContext SessionOperation.Closed
            try {
                val generation = Math.incrementExact(ready.get()?.generation ?: 0L)
                publish(sessionStore.read(), generation, expired = false)
                SessionOperation.Applied
            } catch (failure: IOException) {
                unavailable(failure, SessionStorageFailure.READ)
            }
        }
    }

    fun applyRefreshedTokensIfCurrentBlocking(
        observed: CredentialsSnapshot,
        tokens: TokenPair,
    ): SessionOperation = save(tokens, Change.REFRESH, observed)

    fun expireIfCurrentBlocking(observed: CredentialsSnapshot): SessionOperation = save(null, Change.EXPIRE, observed)

    private fun save(tokens: TokenPair?, change: Change, observed: CredentialsSnapshot? = null): SessionOperation =
        lock.withLock {
            if (closed) return SessionOperation.Closed
            val previous = ready.get()
            if (observed != null && previous?.matches(observed) != true) return SessionOperation.Stale
            val generation = if (change == Change.REFRESH) {
                checkNotNull(previous).generation
            } else {
                Math.incrementExact(previous?.generation ?: 0L)
            }
            try {
                val record = sessionStore.write(tokens)
                publish(record, generation, expired = change == Change.EXPIRE)
                SessionOperation.Applied
            } catch (failure: IOException) {
                unavailable(failure, SessionStorageFailure.WRITE)
            }
        }

    private fun publish(record: SessionRecord, generation: Long, expired: Boolean) {
        val state = when {
            expired -> SessionState.Expired
            record.tokens != null -> SessionState.Authenticated(generation)
            else -> SessionState.Guest
        }
        val revision = Math.incrementExact(ready.get()?.revision ?: 0L)
        ready.set(StoredSession(record, generation, revision, state))
        mutableState.value = state
    }

    private fun unavailable(failure: IOException, fallback: SessionStorageFailure): SessionOperation.Failed {
        val reason = (failure as? SessionStorageException)?.failure ?: fallback
        mutableState.value = SessionState.Unavailable(reason, ready.get()?.state)
        return SessionOperation.Failed(reason)
    }

    companion object {

        suspend fun open(
            sessionStore: SessionStore,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): SessionManager {
            val manager = SessionManager(sessionStore, ioDispatcher)
            try {
                manager.restoreStoredSession()
                return manager
            } catch (cancelled: CancellationException) {
                manager.close()
                throw cancelled
            }
        }
    }
}
