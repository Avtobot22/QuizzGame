package com.quizzgame.core.session

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
}

sealed interface SessionOperation {
    data object Applied : SessionOperation
    data object Stale : SessionOperation
    data object Closed : SessionOperation
    data class Failed(val reason: SessionStorageFailure) : SessionOperation
}
