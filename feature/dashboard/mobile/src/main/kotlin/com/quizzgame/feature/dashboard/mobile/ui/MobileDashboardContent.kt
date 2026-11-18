package com.quizzgame.feature.dashboard.mobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorBanner
import com.quizzgame.feature.dashboard.mobile.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.mobile.presentation.MobileDashboardIntent
import com.quizzgame.feature.dashboard.mobile.presentation.MobileDashboardState

@Composable
internal fun MobileDashboardContent(
    state: MobileDashboardState,
    onIntent: (MobileDashboardIntent) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Dashboard) -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        (state as? MobileDashboardState.FailureWithFallback)?.let { failed ->
            QuizzMobileErrorBanner(failed.error, { action ->
                when (action) {
                    ErrorActionKind.RETRY -> onIntent(MobileDashboardIntent.Retry)
                    ErrorActionKind.DISMISS -> onIntent(MobileDashboardIntent.DismissError)
                    ErrorActionKind.SIGN_IN -> onSignIn()
                    else -> Unit
                }
            })
        }
        content(state.dashboard)
    }
}
