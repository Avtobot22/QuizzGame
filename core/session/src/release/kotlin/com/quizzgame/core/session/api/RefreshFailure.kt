package com.quizzgame.core.session.api

import com.quizzgame.core.network.BackendErrorDetails

sealed interface RefreshFailure {
    data class Http(val status: Int, val backend: BackendErrorDetails? = null) : RefreshFailure {
        override fun toString(): String = "Http(status=$status, backend=${backend != null})"
    }
    data object Io : RefreshFailure
    data object Timeout : RefreshFailure
    data object InvalidResponse : RefreshFailure
}
