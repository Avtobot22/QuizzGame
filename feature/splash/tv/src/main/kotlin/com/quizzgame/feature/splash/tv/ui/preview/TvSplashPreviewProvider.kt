package com.quizzgame.feature.splash.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.feature.splash.tv.presentation.TvSplashUiState

internal class TvSplashPreviewProvider : PreviewParameterProvider<TvSplashUiState> {
    override val values: Sequence<TvSplashUiState> = sequenceOf(
        TvSplashUiState.Loading,
        TvSplashUiState.Error(
            ErrorPresentationSpec(
                ErrorSurface.BLOCKING,
                ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY),
            ),
        ),
    )
}
