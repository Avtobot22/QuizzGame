package com.quizzgame.feature.dashboard.mobile.presentation

internal sealed interface MobileDashboardIntent {
    data object Retry : MobileDashboardIntent
    data object DismissError : MobileDashboardIntent
}
