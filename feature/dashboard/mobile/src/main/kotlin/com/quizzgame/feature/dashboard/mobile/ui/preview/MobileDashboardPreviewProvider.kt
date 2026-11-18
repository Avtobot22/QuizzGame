package com.quizzgame.feature.dashboard.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.feature.dashboard.mobile.domain.entity.localDashboard
import com.quizzgame.feature.dashboard.mobile.presentation.MobileDashboardState

internal class MobileDashboardPreviewProvider : PreviewParameterProvider<MobileDashboardState> {
    override val values = sequenceOf(
        MobileDashboardState.Bootstrapped(),
        MobileDashboardState.Remote(localDashboard()),
        MobileDashboardState.FailureWithFallback(
            ErrorPresentationSpec(
                ErrorSurface.BANNER,
                ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS),
            ),
        ),
    )
}
