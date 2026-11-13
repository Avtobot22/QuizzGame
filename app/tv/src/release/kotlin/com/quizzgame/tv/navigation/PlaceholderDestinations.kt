package com.quizzgame.tv.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvScreenScaffold
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.R

@Composable
@Suppress("UnusedParameter")
internal fun TvSignInDestination(onContinue: () -> Boolean) {
    Text(stringResource(R.string.signin_placeholder))
}

@Composable
@Suppress("UnusedParameter")
internal fun TvDashboardDestination(
    onLogout: () -> Unit,
    graphs: Set<FeatureEntries<DashboardNavigationContext>>,
) {
    QuizzTvScreenScaffold { padding -> Text(stringResource(R.string.dashboard_placeholder), Modifier.padding(padding)) }
}
