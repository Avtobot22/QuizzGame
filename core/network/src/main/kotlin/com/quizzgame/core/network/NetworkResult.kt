package com.quizzgame.core.network

import kotlinx.serialization.Serializable

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T> {

        override fun toString(): String = "Success(value=<redacted>)"
    }

    data class Failure(val reason: NetworkFailure) : NetworkResult<Nothing>
}

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

@Serializable
data class BackendErrorDetails(
    val reasonCode: Int,
    val debugMessage: String,
    val userFriendlyMessage: String? = null,
) {

    override fun toString(): String = "BackendErrorDetails(<redacted>)"
}

enum class MockFailureReason {

    NO_MATCHING_RULE,
    INVALID_REQUEST,
    CALLBACK_FAILURE,
    INVALID_RESPONSE,
}
