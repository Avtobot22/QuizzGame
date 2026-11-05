package com.quizzgame.core.network.result

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.auth.RefreshFailureReason

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
    data class RefreshUncertain(val reason: RefreshFailureReason) : NetworkFailure
    data class CredentialsUnavailable(val kind: CredentialUnavailableKind) : NetworkFailure
}
