package com.quizzgame.feature.splash.mobile.presentation

internal sealed interface MobileSplashIntent {
    data object Retry : MobileSplashIntent
    data class NavigationHandled(val requestId: String) : MobileSplashIntent
    data object SignIn : MobileSplashIntent
}
