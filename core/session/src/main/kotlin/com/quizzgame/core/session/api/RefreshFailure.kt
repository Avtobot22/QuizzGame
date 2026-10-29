package com.quizzgame.core.session.api

import java.io.IOException

sealed interface RefreshFailure {
    data class Http(val status: Int) : RefreshFailure
    data object Io : RefreshFailure
    data object Timeout : RefreshFailure
    data object InvalidResponse : RefreshFailure
}

class RefreshException(val failure: RefreshFailure) : IOException("Refresh request failed: $failure")
