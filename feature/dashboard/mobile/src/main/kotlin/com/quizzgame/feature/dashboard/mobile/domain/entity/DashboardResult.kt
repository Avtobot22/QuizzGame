package com.quizzgame.feature.dashboard.mobile.domain.entity

import com.quizzgame.core.error.failure.AppFailure

internal sealed interface DashboardResult {
    data class Loaded(val content: Dashboard) : DashboardResult
    data class Failed(val failure: AppFailure) : DashboardResult
}
