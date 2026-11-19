package com.quizzgame.feature.account.tv.presentation

import com.quizzgame.shared.splash.domain.entity.AccountProfile

internal sealed interface TvProfileState {
    data object Loading : TvProfileState
    data class Loaded(val profile: AccountProfile, val refreshing: Boolean = false) : TvProfileState
}
