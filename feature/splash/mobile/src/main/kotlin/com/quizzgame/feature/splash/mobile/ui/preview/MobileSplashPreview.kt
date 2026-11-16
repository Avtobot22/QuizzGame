package com.quizzgame.feature.splash.mobile.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashUiState
import com.quizzgame.feature.splash.mobile.ui.MobileSplashContent

@Preview(name = "MobileSplash", locale = "ru", widthDp = 360, heightDp = 800)
@Preview(name = "MobileSplash EN", locale = "en", widthDp = 360, heightDp = 800)
@Composable
private fun MobileSplashPreview(@PreviewParameter(MobileSplashPreviewProvider::class) state: MobileSplashUiState) {
    QuizzMobilePreviewScreen {
        MobileSplashContent(state, {}, { false }, { false }, {}, Modifier.fillMaxSize())
    }
}
