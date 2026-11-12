package com.quizzgame.feature.splash.mobile.presentation

internal sealed interface MobileSplashIntent {
    data object Retry : MobileSplashIntent
    data object NavigationHandled : MobileSplashIntent
    data object SignIn : MobileSplashIntent
}
