package com.quizzgame.feature.feed.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen

@Preview(name = "Feed light", locale = "ru", widthDp = 390, heightDp = 844)
@Preview(name = "Feed dark", locale = "ru", widthDp = 390, heightDp = 844, uiMode = 32)
@Preview(name = "Feed wide", locale = "en", widthDp = 1000, heightDp = 800)
@Preview(name = "Feed large font", locale = "ru", widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun MobileFeedPreview(@PreviewParameter(MobileFeedPreviewProvider::class) state: MobileFeedPreviewState) {
    QuizzMobilePreviewScreen { MobileFeedPreviewStateContent(state) }
}
