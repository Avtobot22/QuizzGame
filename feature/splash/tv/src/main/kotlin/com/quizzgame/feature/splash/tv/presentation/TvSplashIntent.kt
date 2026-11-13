package com.quizzgame.feature.splash.tv.presentation

internal sealed interface TvSplashIntent {
    data object Retry : TvSplashIntent
    data object NavigationHandled : TvSplashIntent
    data object SignIn : TvSplashIntent
}
