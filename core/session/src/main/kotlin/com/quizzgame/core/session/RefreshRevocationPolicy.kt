package com.quizzgame.core.session

import com.quizzgame.core.session.api.RefreshFailure

fun interface RefreshRevocationPolicy {

    fun isRevoked(failure: RefreshFailure): Boolean

    companion object {

        fun never(): RefreshRevocationPolicy = RefreshRevocationPolicy { false }
    }
}
