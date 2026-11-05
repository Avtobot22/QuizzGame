package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.flow.StateFlow

interface SessionActions {
    val state: StateFlow<SessionState>
    val recoveryAction: StateFlow<SessionRecoveryAction>
    suspend fun signIn(tokens: TokenPair): SessionChangeResult
    suspend fun signOut(): SessionChangeResult
    suspend fun recoverStorage(): SessionRecoveryResult
}

sealed interface SessionChangeResult {
    data object Committed : SessionChangeResult
    data object BusyOrBlocked : SessionChangeResult
    data class NeedsStorageRecovery(val reason: SessionStorageFailure) : SessionChangeResult
    data object Closed : SessionChangeResult
}

sealed interface SessionRecoveryResult {
    data object Recovered : SessionRecoveryResult
    data object NotAllowed : SessionRecoveryResult
    data class NeedsStorageRecovery(val reason: SessionStorageFailure) : SessionRecoveryResult
    data object ReauthenticationRequired : SessionRecoveryResult
    data object Closed : SessionRecoveryResult
}

enum class SessionRecoveryAction {
    NONE,
    RETRY_READ,
    RETRY_PERSISTENCE,
    REAUTHENTICATE,
}
