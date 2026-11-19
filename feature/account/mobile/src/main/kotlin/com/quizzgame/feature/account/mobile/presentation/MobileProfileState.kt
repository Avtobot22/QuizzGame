package com.quizzgame.feature.account.mobile.presentation

import com.quizzgame.shared.splash.domain.entity.AccountProfile

internal sealed interface MobileProfileState {
    data object Loading : MobileProfileState
    data class Ready(val profile: AccountProfile, val refreshing: Boolean = false) : MobileProfileState
}
