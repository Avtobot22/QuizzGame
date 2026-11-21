package com.quizzgame.feature.account.tv.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.account.tv.ui.TvProfileContent
import com.quizzgame.shared.account.presentation.ProfileState

@Preview(locale = "ru", widthDp = 960, heightDp = 540)
@Composable
private fun TvProfilePreview(@PreviewParameter(TvProfilePreviewProvider::class) state: ProfileState) {
    QuizzTvPreviewScreen { TvProfileContent(state, {}, {}, {}, {}) }
}
