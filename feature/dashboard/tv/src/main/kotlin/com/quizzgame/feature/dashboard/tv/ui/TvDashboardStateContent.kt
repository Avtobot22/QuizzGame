package com.quizzgame.feature.dashboard.tv.ui

import androidx.compose.runtime.Composable
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.dashboard.tv.navigation.DashboardContext
import com.quizzgame.feature.dashboard.tv.presentation.TvDashboardIntent
import com.quizzgame.feature.dashboard.tv.presentation.TvDashboardState

@Composable
internal fun TvDashboardStateContent(
    state: TvDashboardState,
    onIntent: (TvDashboardIntent) -> Unit,
    graphs: Set<FeatureEntries<DashboardContext>>,
    onLogout: () -> Unit,
    onSignIn: () -> Unit,
) {
    when (val current = state) {
        TvDashboardState.Loading -> TvDashboardLoading()
        TvDashboardState.Empty -> TvDashboardEmpty(onRetry = { onIntent(TvDashboardIntent.Retry) })
        is TvDashboardState.Error -> TvDashboardError(
            current.spec,
            { onIntent(TvDashboardIntent.Retry) },
            onSignIn,
        )
        is TvDashboardState.Content -> TvDashboardContent(current.value, graphs, onLogout)
    }
}
