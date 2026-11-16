package com.quizzgame.feature.dashboard.tv.presentation

internal sealed interface TvDashboardIntent {
    data object Retry : TvDashboardIntent
}
