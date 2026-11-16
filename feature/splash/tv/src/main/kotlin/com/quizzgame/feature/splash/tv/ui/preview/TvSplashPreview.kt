package com.quizzgame.feature.splash.tv.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.splash.tv.presentation.TvSplashUiState
import com.quizzgame.feature.splash.tv.ui.TvSplashContent

@Preview(name = "TvSplash", locale = "ru", widthDp = 960, heightDp = 540)
@Preview(name = "TvSplash EN", locale = "en", widthDp = 960, heightDp = 540)
@Composable
private fun TvSplashPreview(@PreviewParameter(TvSplashPreviewProvider::class) state: TvSplashUiState) {
    QuizzTvPreviewScreen {
        TvSplashContent(state, {}, { false }, { false }, {}, Modifier.fillMaxSize())
    }
}
