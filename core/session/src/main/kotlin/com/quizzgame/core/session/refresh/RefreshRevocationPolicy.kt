package com.quizzgame.core.session.refresh

import com.quizzgame.core.session.refresh.api.RefreshFailure

fun interface RefreshRevocationPolicy {

    fun isRevoked(failure: RefreshFailure): Boolean

    companion object {

        fun never(): RefreshRevocationPolicy = RefreshRevocationPolicy { false }
    }
}
