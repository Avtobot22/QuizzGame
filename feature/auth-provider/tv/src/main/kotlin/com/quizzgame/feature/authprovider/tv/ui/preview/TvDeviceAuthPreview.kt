package com.quizzgame.feature.authprovider.tv.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.authprovider.tv.presentation.TvDeviceAuthState
import com.quizzgame.feature.authprovider.tv.ui.TvDeviceAuthStage

@Preview(locale = "ru", widthDp = 960, heightDp = 540)
@Composable
private fun TvDeviceAuthPreview(
    @PreviewParameter(TvDeviceAuthPreviewProvider::class) state: TvDeviceAuthState,
) {
    QuizzTvPreviewScreen {
        TvDeviceAuthStage(state, onRetry = {}, onBack = {})
    }
}
