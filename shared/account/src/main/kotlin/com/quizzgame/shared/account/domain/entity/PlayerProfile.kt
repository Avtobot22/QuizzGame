package com.quizzgame.shared.account.domain.entity

import com.quizzgame.shared.splash.domain.entity.CurrentUser

sealed interface PlayerProfile {
    data object Guest : PlayerProfile
    data class SignedIn(val generation: Long, val user: CurrentUser?) : PlayerProfile
    data object Unavailable : PlayerProfile
}
