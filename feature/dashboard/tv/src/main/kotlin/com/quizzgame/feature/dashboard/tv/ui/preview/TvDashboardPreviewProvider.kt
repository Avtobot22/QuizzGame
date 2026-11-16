package com.quizzgame.feature.dashboard.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.feature.dashboard.tv.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTab
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.domain.entity.NavigationAppearance
import com.quizzgame.feature.dashboard.tv.presentation.TvDashboardState

internal class TvDashboardPreviewProvider : PreviewParameterProvider<TvDashboardState> {
    override val values: Sequence<TvDashboardState> = sequenceOf(
        TvDashboardState.Loading,
        TvDashboardState.Empty,
        TvDashboardState.Error(
            ErrorPresentationSpec(
                ErrorSurface.BLOCKING,
                ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY),
            ),
        ),
        TvDashboardState.Content(
            Dashboard(
                tabs = DashboardTabId.entries.map { id ->
                    DashboardTab(
                        id,
                        when (id) {
                            DashboardTabId.FEED -> "Главная"
                            DashboardTabId.LIBRARY -> "Библиотека"
                            DashboardTabId.SETTINGS -> "Настройки"
                        },
                        id != DashboardTabId.SETTINGS,
                        null,
                        MediaSpec.Placeholder,
                        null,
                    )
                },
                appearance = NavigationAppearance("solid", null, null, null),
            ),
        ),
    )
}
