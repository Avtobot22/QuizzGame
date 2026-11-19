package com.quizzgame.feature.dashboard.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import com.quizzgame.feature.dashboard.mobile.ui.MobileAccountContent

@Preview(name = "Account light", locale = "ru", widthDp = 390, heightDp = 844)
@Preview(name = "Account dark", locale = "ru", widthDp = 390, heightDp = 844, uiMode = 32)
@Preview(name = "Account large font", locale = "en", widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun MobileAccountPreview(
    @PreviewParameter(
        MobileAccountPreviewProvider::class,
    ) state: Account,
) {
    QuizzMobilePreviewScreen { MobileAccountContent(state, {}, {}) }
}
