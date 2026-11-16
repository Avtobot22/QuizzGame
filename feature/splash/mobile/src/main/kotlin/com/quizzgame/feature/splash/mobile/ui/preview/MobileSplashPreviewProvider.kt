package com.quizzgame.feature.splash.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashUiState

internal class MobileSplashPreviewProvider : PreviewParameterProvider<MobileSplashUiState> {
    override val values: Sequence<MobileSplashUiState> = sequenceOf(
        MobileSplashUiState.Loading,
        MobileSplashUiState.Error(
            ErrorPresentationSpec(
                ErrorSurface.BLOCKING,
                ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY),
            ),
        ),
    )
}
