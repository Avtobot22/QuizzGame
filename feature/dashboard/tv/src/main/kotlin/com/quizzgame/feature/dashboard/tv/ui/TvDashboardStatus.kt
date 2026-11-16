package com.quizzgame.feature.dashboard.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.tv.QuizzTvCatalogSkeleton
import com.quizzgame.core.designsystem.tv.media.QuizzTvEmptyState
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorFullScreen
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorInline
import com.quizzgame.feature.dashboard.tv.R
import com.quizzgame.feature.dashboard.tv.ui.tags.TvDashboardTags
import com.quizzgame.core.error.ui.common.R as ErrorR
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvDashboardLoading() {
    QuizzTvCatalogSkeleton(
        stringResource(UiR.string.quizz_loading),
        Modifier.testTag(TvDashboardTags.LOADING),
        showNavigation = true,
    )
}

@Composable
internal fun TvDashboardEmpty(onRetry: () -> Unit, requestFocus: Boolean = true) {
    QuizzTvEmptyState(
        stringResource(R.string.dashboard_empty),
        stringResource(ErrorR.string.quizz_error_action_retry),
        onRetry,
        Modifier.testTag(TvDashboardTags.EMPTY),
        description = stringResource(R.string.dashboard_empty_description),
        requestFocus = requestFocus,
    )
}

@Composable
internal fun TvDashboardError(spec: ErrorPresentationSpec, onRetry: () -> Unit, onSignIn: () -> Unit = {}) {
    if (spec.surface == ErrorSurface.BLOCKING) {
        QuizzTvErrorFullScreen(spec, onAction = { action ->
            when (action) {
                ErrorActionKind.RETRY -> onRetry()
                ErrorActionKind.SIGN_IN -> onSignIn()
                else -> Unit
            }
        })
    } else {
        QuizzTvErrorInline(spec, onAction = { action ->
            when (action) {
                ErrorActionKind.RETRY -> onRetry()
                ErrorActionKind.SIGN_IN -> onSignIn()
                else -> Unit
            }
        })
    }
}
