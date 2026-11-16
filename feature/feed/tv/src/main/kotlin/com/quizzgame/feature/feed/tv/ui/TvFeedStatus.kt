package com.quizzgame.feature.feed.tv.ui

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
import com.quizzgame.feature.feed.tv.R
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.core.error.ui.common.R as ErrorR
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvFeedLoading() {
    QuizzTvCatalogSkeleton(stringResource(UiR.string.quizz_loading), Modifier.testTag(TvFeedTags.LOADING))
}

@Composable
internal fun TvFeedEmpty(onRetry: () -> Unit, requestFocus: Boolean = true) {
    QuizzTvEmptyState(
        stringResource(R.string.feed_empty),
        stringResource(ErrorR.string.quizz_error_action_retry),
        onRetry,
        Modifier.testTag(TvFeedTags.EMPTY),
        description = stringResource(R.string.feed_empty_description),
        requestFocus = requestFocus,
    )
}

@Composable
internal fun TvFeedError(spec: ErrorPresentationSpec, onRetry: () -> Unit, onSignIn: () -> Unit = {}) {
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
