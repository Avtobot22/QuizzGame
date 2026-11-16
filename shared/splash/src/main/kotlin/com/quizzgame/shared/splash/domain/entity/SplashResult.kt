package com.quizzgame.shared.splash.domain.entity

import com.quizzgame.core.error.failure.AppFailure

sealed interface SplashResult {
    data object Continue : SplashResult
    data object SignIn : SplashResult
    data class Failed(val failure: AppFailure) : SplashResult
}
