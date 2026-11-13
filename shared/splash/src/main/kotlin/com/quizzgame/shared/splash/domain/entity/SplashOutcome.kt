package com.quizzgame.shared.splash.domain.entity

import com.quizzgame.core.error.failure.AppFailure

sealed interface SplashOutcome {
    data object Continue : SplashOutcome
    data object SignIn : SplashOutcome
    data class Failed(val failure: AppFailure) : SplashOutcome
}
