package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableException
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.storage.SessionStorageFailure

sealed interface SessionState {
    sealed interface Ready : SessionState
    data object Initializing : SessionState
    data object Closed : SessionState
    data object Guest : Ready
    data class Authenticated(val sessionGeneration: Long) : Ready
    data object Expired : Ready
    data class Unavailable(
        val reason: SessionStorageFailure,
        val lastKnown: Ready? = null,
    ) : SessionState
    data class Blocked(
        val kind: CredentialUnavailableKind,
        val lastKnown: Ready? = null,
    ) : SessionState
}

sealed interface SessionOperation {
    data object Applied : SessionOperation
    data object Stale : SessionOperation
    data object Closed : SessionOperation
    data object Busy : SessionOperation
    data class Failed(val reason: SessionStorageFailure) : SessionOperation
}

class SessionUnavailableException(kind: CredentialUnavailableKind = CredentialUnavailableKind.NOT_INITIALIZED) :
    CredentialUnavailableException(kind)
