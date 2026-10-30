package com.quizzgame.core.network

import kotlinx.serialization.Serializable

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T> {

        override fun toString(): String = "Success(value=<redacted>)"
    }

    data class Failure(val reason: NetworkFailure) : NetworkResult<Nothing>
}

@Serializable
data class BackendErrorDetails(
    val reasonCode: Int,
    val debugMessage: String,
    val userFriendlyMessage: String? = null,
) {

    override fun toString(): String = "BackendErrorDetails(<redacted>)"
}
