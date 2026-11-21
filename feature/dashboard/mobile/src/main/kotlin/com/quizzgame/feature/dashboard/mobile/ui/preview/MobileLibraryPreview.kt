package com.quizzgame.feature.dashboard.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.dashboard.mobile.ui.MobileLibraryScreen

@Preview(locale = "ru", widthDp = 390, heightDp = 760)
@Composable
private fun MobileLibraryPreview() {
    QuizzMobilePreviewScreen { MobileLibraryScreen() }
}
