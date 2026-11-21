package com.quizzgame.feature.play.mobile.presentation

internal sealed interface MobilePlayJoinIdentity {
    data object Checking : MobilePlayJoinIdentity
    data object Guest : MobilePlayJoinIdentity
    data class Account(
        val generation: Long,
        val profileName: String?,
        val refreshing: Boolean = false,
    ) : MobilePlayJoinIdentity {
        override fun toString(): String = "Account(generation=$generation)"
    }
}
