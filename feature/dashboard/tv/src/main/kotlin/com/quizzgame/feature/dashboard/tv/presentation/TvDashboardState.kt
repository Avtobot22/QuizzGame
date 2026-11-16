package com.quizzgame.feature.dashboard.tv.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.feature.dashboard.tv.domain.entity.Dashboard

internal sealed interface TvDashboardState {
    data object Loading : TvDashboardState
    data class Content(val value: Dashboard) : TvDashboardState
    data object Empty : TvDashboardState
    data class Error(val spec: ErrorPresentationSpec) : TvDashboardState
}
