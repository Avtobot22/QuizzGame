package com.quizzgame.feature.auth.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.feature.auth.mobile.ui.MobileSignInContent
import com.quizzgame.shared.auth.domain.entity.OtpState

@Preview(locale = "ru", widthDp = 390, heightDp = 760, showBackground = true)
@Composable
private fun MobileSignInPreview(@PreviewParameter(MobileSignInPreviewProvider::class) state: OtpState) {
    QuizzMobileTheme { MobileSignInContent(state, {}, {}, recovery = false) }
}
