package com.quizzgame.feature.dashboard.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.dashboard.mobile.presentation.MobileDashboardState
import com.quizzgame.feature.dashboard.mobile.ui.MobileDashboardContent
import com.quizzgame.feature.dashboard.mobile.ui.MobileLibraryScreen

@Preview(name = "Dashboard shell light", locale = "ru", widthDp = 390, heightDp = 844)
@Preview(name = "Dashboard shell dark", locale = "ru", widthDp = 390, heightDp = 844, uiMode = 32)
@Composable
private fun MobileDashboardPreview(
    @PreviewParameter(
        MobileDashboardPreviewProvider::class,
    ) state: MobileDashboardState,
) {
    QuizzMobilePreviewScreen {
        MobileDashboardContent(state, {}, {}) { MobileLibraryScreen() }
    }
}
