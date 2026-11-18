package com.quizzgame.feature.dashboard.mobile.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.feature.dashboard.mobile.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.mobile.domain.entity.localDashboard

internal sealed interface MobileDashboardState {
    val dashboard: Dashboard
    data class Bootstrapped(override val dashboard: Dashboard = localDashboard()) : MobileDashboardState
    data class Remote(override val dashboard: Dashboard) : MobileDashboardState
    data class FailureWithFallback(
        val error: ErrorPresentationSpec,
        override val dashboard: Dashboard = localDashboard(),
    ) : MobileDashboardState
}
