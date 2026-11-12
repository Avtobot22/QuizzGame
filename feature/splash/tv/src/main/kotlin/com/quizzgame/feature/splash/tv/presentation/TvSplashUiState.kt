package com.quizzgame.feature.splash.tv.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import java.util.UUID

internal sealed interface TvSplashUiState {
    data object Loading : TvSplashUiState
    data class Failed(val spec: ErrorPresentationSpec) : TvSplashUiState
    data class NavigationPending(
        val destination: TvSplashDestination,
        val requestId: String = UUID.randomUUID().toString(),
    ) : TvSplashUiState
    data object NavigationComplete : TvSplashUiState
}
