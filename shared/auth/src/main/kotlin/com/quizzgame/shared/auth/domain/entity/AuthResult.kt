package com.quizzgame.shared.auth.domain.entity

import com.quizzgame.core.error.failure.AppFailure

sealed interface AuthResult<out T> {
    class Success<T>(val value: T) : AuthResult<T> {
        override fun toString(): String = "AuthResult.Success(<redacted>)"
    }
    data class Failed(val failure: AuthFailure) : AuthResult<Nothing>
}

sealed interface AuthFailure {
    data class General(val failure: AppFailure) : AuthFailure
    data class RateLimited(val retryAfterSeconds: Long?) : AuthFailure
    data object InvalidIdentifier : AuthFailure
    data object InvalidCode : AuthFailure
    data object Expired : AuthFailure
    data object Consumed : AuthFailure
    data object ProviderRejected : AuthFailure
    data object ProviderUnavailable : AuthFailure
    data object SessionChanged : AuthFailure
    data object StorageRecovery : AuthFailure
}
