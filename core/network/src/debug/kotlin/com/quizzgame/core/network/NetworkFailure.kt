package com.quizzgame.core.network

sealed interface NetworkFailure {
    data class Http(val status: Int, val backend: BackendErrorDetails? = null) : NetworkFailure {

        override fun toString(): String = "Http(status=$status, backend=${backend != null})"
    }

    data object Connection : NetworkFailure
    data object Timeout : NetworkFailure
    data object InvalidResponse : NetworkFailure
    data object AuthenticationRequired : NetworkFailure
    data object SessionRevoked : NetworkFailure
    data class RefreshFailed(val reason: RefreshFailureReason) : NetworkFailure
    data class MockConfiguration(val reason: MockFailureReason) : NetworkFailure
}
