package com.quizzgame.feature.quizentry.tv.ui.status

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.QuizzTvDetailSkeleton
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorFullScreen
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorInline
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.quizentry.tv.ui.TvQuizEntryBack
import com.quizzgame.feature.quizentry.tv.ui.tags.TvQuizEntryTags
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvQuizEntryLoading(onBack: () -> Unit) {
    Box {
        QuizzTvDetailSkeleton(stringResource(UiR.string.quizz_loading), Modifier.testTag(TvQuizEntryTags.LOADING))
        TvQuizEntryBack(onBack, Modifier.padding(QuizzTvLayout.screenInset.dp))
    }
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
