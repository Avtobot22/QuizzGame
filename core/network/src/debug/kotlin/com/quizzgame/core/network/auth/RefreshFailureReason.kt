package com.quizzgame.core.network.auth

import com.quizzgame.core.network.result.BackendErrorDetails
import com.quizzgame.core.network.result.NetworkFailure

sealed interface RefreshFailureReason {
    data class Http(val status: Int, val backend: BackendErrorDetails? = null) : RefreshFailureReason {
        override fun toString(): String = "Http(status=$status, backend=${backend != null})"
    }
    data class Diagnostic(val failure: NetworkFailure.MockConfiguration) : RefreshFailureReason
    data object Io : RefreshFailureReason
    data object Timeout : RefreshFailureReason
    data object InvalidResponse : RefreshFailureReason
    data object Storage : RefreshFailureReason
}
