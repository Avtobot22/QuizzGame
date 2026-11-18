package com.quizzgame.feature.auth.tv.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.feature.auth.tv.ui.TvSignInContent
import com.quizzgame.shared.auth.domain.entity.OtpState

@Preview(locale = "ru", widthDp = 960, heightDp = 540, showBackground = true)
@Composable
private fun TvSignInPreview(@PreviewParameter(TvSignInPreviewProvider::class) state: OtpState) {
    QuizzTvTheme { TvSignInContent(state, {}, {}, recovery = false) }
}
