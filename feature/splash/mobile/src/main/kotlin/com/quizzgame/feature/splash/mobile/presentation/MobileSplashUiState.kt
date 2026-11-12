package com.quizzgame.feature.splash.mobile.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec

internal sealed interface MobileSplashUiState {
    data object Loading : MobileSplashUiState
    data class Failed(val spec: ErrorPresentationSpec) : MobileSplashUiState
    data class NavigationPending(val destination: MobileSplashDestination) : MobileSplashUiState
}
