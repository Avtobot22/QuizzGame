package com.quizzgame.feature.splash.tv.presentation

internal sealed interface TvSplashIntent {
    data object Retry : TvSplashIntent
    data class NavigationHandled(val requestId: String) : TvSplashIntent
    data object SignIn : TvSplashIntent
}
