package com.quizzgame.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.mobile.navigation.MobileSignInDestination

@Preview(name = "Sign in", locale = "ru", widthDp = 390, heightDp = 844)
@Composable
private fun SignInPreview() {
    QuizzMobilePreviewScreen { MobileSignInDestination() }
}
