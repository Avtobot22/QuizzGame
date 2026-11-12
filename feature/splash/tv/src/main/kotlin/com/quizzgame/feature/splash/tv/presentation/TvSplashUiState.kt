package com.quizzgame.feature.splash.tv.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec

internal sealed interface TvSplashUiState {
    data object Loading : TvSplashUiState
    data class Failed(val spec: ErrorPresentationSpec) : TvSplashUiState
    data class NavigationPending(val destination: TvSplashDestination) : TvSplashUiState
}
