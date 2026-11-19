package com.quizzgame.feature.auth.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.auth.mobile.ui.MobileSignInContent
import com.quizzgame.shared.auth.presentation.OtpState

@Preview(locale = "ru", widthDp = 390, heightDp = 760, showBackground = true)
@Preview(locale = "en", widthDp = 360, heightDp = 760, fontScale = 1.5f, uiMode = 32)
@Composable
private fun MobileSignInPreview(@PreviewParameter(MobileSignInPreviewProvider::class) state: OtpState) {
    QuizzMobilePreviewScreen { MobileSignInContent(state, {}, {}, recovery = false, previewElapsedMillis = 0) }
}
