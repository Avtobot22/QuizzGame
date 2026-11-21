package com.quizzgame.feature.auth.tv.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.auth.tv.presentation.TvSignInState
import com.quizzgame.feature.auth.tv.ui.TvSignInContent

@Preview(locale = "ru", widthDp = 960, heightDp = 540)
@Composable
private fun TvSignInPreview(@PreviewParameter(TvSignInPreviewProvider::class) state: TvSignInState) {
    QuizzTvPreviewScreen {
        TvSignInContent(state, {}, {}, {}, recovery = false, previewElapsedMillis = 0)
    }
}
