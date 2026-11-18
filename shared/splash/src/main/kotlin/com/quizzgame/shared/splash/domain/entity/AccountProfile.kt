package com.quizzgame.shared.splash.domain.entity

sealed interface AccountProfile {
    data object Guest : AccountProfile
    data class SignedIn(val user: CurrentUser?) : AccountProfile
    data object Unavailable : AccountProfile
}
