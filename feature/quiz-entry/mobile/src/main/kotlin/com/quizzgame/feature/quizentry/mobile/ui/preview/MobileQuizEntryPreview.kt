package com.quizzgame.feature.quizentry.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryState
import com.quizzgame.feature.quizentry.mobile.ui.MobileQuizEntryStateContent

@Preview(name = "QuizEntry light", locale = "ru", widthDp = 390, heightDp = 844)
@Preview(name = "QuizEntry dark", locale = "ru", widthDp = 390, heightDp = 844, uiMode = 32)
@Preview(name = "QuizEntry wide", locale = "en", widthDp = 1000, heightDp = 800)
@Preview(name = "QuizEntry large font", locale = "ru", widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun MobileQuizEntryPreview(
    @PreviewParameter(MobileQuizEntryPreviewProvider::class) state: MobileQuizEntryState,
) {
    QuizzMobilePreviewScreen { MobileQuizEntryStateContent(state, {}, {}, {}) }
}
