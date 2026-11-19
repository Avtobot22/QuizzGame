package com.quizzgame.feature.account.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.account.mobile.presentation.MobileProfileState
import com.quizzgame.feature.account.mobile.ui.MobileProfileContent

@Preview(locale = "ru", widthDp = 390, heightDp = 800)
@Preview(locale = "en", widthDp = 360, heightDp = 800, fontScale = 1.5f, uiMode = 32)
@Composable
private fun MobileProfilePreview(
    @PreviewParameter(MobileProfilePreviewProvider::class) state: MobileProfileState,
) {
    QuizzMobilePreviewScreen { MobileProfileContent(state, {}, {}, {}) }
}
