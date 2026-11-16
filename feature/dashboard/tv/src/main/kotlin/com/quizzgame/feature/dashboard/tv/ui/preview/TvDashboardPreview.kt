package com.quizzgame.feature.dashboard.tv.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.QuizzTvMessageScreen
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.dashboard.tv.R
import com.quizzgame.feature.dashboard.tv.navigation.DashboardContext
import com.quizzgame.feature.dashboard.tv.navigation.DashboardTabRoute
import com.quizzgame.feature.dashboard.tv.presentation.TvDashboardState
import com.quizzgame.feature.dashboard.tv.ui.TvDashboardStateContent

@Preview(name = "TvDashboard", locale = "ru", widthDp = 960, heightDp = 540)
@Preview(name = "TvDashboard EN", locale = "en", widthDp = 960, heightDp = 540)
@Composable
private fun TvDashboardPreview(@PreviewParameter(TvDashboardPreviewProvider::class) state: TvDashboardState) {
    QuizzTvPreviewScreen {
        val graphs = remember {
            setOf(
                FeatureEntries<DashboardContext> { scope, _ ->
                    scope.entry<DashboardTabRoute> {
                        QuizzTvMessageScreen(
                            stringResource(R.string.dashboard_preview_title),
                            stringResource(R.string.dashboard_preview_message),
                        )
                    }
                },
            )
        }
        TvDashboardStateContent(state, {}, graphs, {}, {})
    }
}
