package com.quizzgame.tv.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Text
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.R

@Composable
@Suppress("UnusedParameter")
internal fun TvSignInDestination(onContinue: () -> Unit) {
    Text(stringResource(R.string.signin_placeholder))
}

@Composable
@Suppress("UnusedParameter")
internal fun TvDashboardDestination(
    onLogout: () -> Unit,
    graphs: Set<FeatureEntries<DashboardNavigationContext>>,
) {
    Text(stringResource(R.string.dashboard_placeholder))
}
