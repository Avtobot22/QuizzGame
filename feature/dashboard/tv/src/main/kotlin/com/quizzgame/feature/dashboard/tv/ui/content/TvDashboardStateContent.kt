package com.quizzgame.feature.dashboard.tv.ui.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.dashboard.tv.R
import com.quizzgame.feature.dashboard.tv.navigation.DashboardContext
import com.quizzgame.feature.dashboard.tv.presentation.TvDashboardIntent
import com.quizzgame.feature.dashboard.tv.presentation.TvDashboardState
import com.quizzgame.feature.dashboard.tv.ui.status.TvDashboardEmpty
import com.quizzgame.feature.dashboard.tv.ui.status.TvDashboardError
import com.quizzgame.feature.dashboard.tv.ui.status.TvDashboardLoading
import com.quizzgame.feature.dashboard.tv.ui.tags.TvDashboardTags

@Composable
internal fun TvDashboardStateContent(
    state: TvDashboardState,
    onIntent: (TvDashboardIntent) -> Unit,
    graphs: Set<FeatureEntries<DashboardContext>>,
    onProfile: () -> Unit,
    onSignIn: () -> Unit,
) {
    if (state is TvDashboardState.Content) {
        TvDashboardContent(state.value, graphs, onProfile)
    } else {
        Box(Modifier.fillMaxSize()) {
            when (state) {
                TvDashboardState.Loading -> TvDashboardLoading()
                TvDashboardState.Empty -> TvDashboardEmpty(onRetry = { onIntent(TvDashboardIntent.Retry) })
                is TvDashboardState.Error -> TvDashboardError(
                    state.spec,
                    { onIntent(TvDashboardIntent.Retry) },
                    onSignIn,
                )
                is TvDashboardState.Content -> Unit
            }
            QuizzTvButton(
                onProfile,
                Modifier.align(Alignment.TopEnd)
                    .padding(horizontal = QuizzTvLayout.screenInset.dp, vertical = QuizzTvLayout.verticalInset.dp)
                    .testTag(TvDashboardTags.PROFILE),
                style = QuizzTvButtonStyle.QUIET,
            ) { Text(stringResource(R.string.dashboard_profile)) }
        }
    }
}
