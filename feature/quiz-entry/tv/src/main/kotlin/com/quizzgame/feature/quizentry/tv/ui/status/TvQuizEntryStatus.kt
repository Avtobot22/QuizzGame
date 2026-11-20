package com.quizzgame.feature.quizentry.tv.ui.status

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.tv.QuizzTvDetailSkeleton
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorFullScreen
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorInline
import com.quizzgame.feature.quizentry.tv.ui.tags.TvQuizEntryTags
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvQuizEntryLoading() {
    QuizzTvDetailSkeleton(stringResource(UiR.string.quizz_loading), Modifier.testTag(TvQuizEntryTags.LOADING))
}

@Composable
internal fun TvQuizEntryError(
    spec: ErrorPresentationSpec,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    val onAction: (ErrorActionKind) -> Unit = { action ->
        when (action) {
            ErrorActionKind.RETRY -> onRetry()
            ErrorActionKind.SIGN_IN -> onSignIn()
            ErrorActionKind.GO_BACK -> onBack()
            else -> Unit
        }
    }
    if (spec.surface == ErrorSurface.BLOCKING) {
        QuizzTvErrorFullScreen(
            spec.copy(actions = (spec.actions + ErrorActionKind.GO_BACK).distinct()),
            onAction = onAction,
        )
    } else {
        QuizzTvErrorInline(spec, onAction = onAction)
    }
}
