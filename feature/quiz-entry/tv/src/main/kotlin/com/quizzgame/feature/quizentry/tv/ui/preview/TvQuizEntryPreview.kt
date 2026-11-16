package com.quizzgame.feature.quizentry.tv.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryState
import com.quizzgame.feature.quizentry.tv.ui.TvQuizEntryStateContent

@Preview(name = "TvQuizEntry", locale = "ru", widthDp = 960, heightDp = 540)
@Preview(name = "TvQuizEntry EN", locale = "en", widthDp = 960, heightDp = 540)
@Composable
private fun TvQuizEntryPreview(@PreviewParameter(TvQuizEntryPreviewProvider::class) state: TvQuizEntryState) {
    QuizzTvPreviewScreen {
        TvQuizEntryStateContent(state, {}, {}, Modifier.fillMaxSize(), {})
    }
}
