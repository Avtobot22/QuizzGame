package com.quizzgame.core.network

sealed interface RefreshFailureReason {
    data class Http(val status: Int, val backend: BackendErrorDetails? = null) : RefreshFailureReason {
        override fun toString(): String = "Http(status=$status, backend=${backend != null})"
    }
    data object Io : RefreshFailureReason
    data object Timeout : RefreshFailureReason
    data object InvalidResponse : RefreshFailureReason
    data object Storage : RefreshFailureReason
}
