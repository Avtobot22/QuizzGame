package com.quizzgame.feature.play.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.ui.MobilePlayContent

@Preview(name = "Play light", locale = "ru", widthDp = 390, heightDp = 844)
@Preview(name = "Play dark", locale = "ru", widthDp = 390, heightDp = 844, uiMode = 32)
@Preview(name = "Play tablet", locale = "ru", widthDp = 1000, heightDp = 800)
@Preview(name = "Play large font", locale = "en", widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun PlayPreview(@PreviewParameter(MobilePlayPreviewProvider::class) state: MobilePlayState) {
    QuizzMobilePreviewScreen { MobilePlayContent(state, {}) }
}
