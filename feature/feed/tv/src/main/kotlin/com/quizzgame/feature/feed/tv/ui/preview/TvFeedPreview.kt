package com.quizzgame.feature.feed.tv.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import com.quizzgame.feature.feed.tv.ui.TvFeedStateContent

@Preview(name = "TvFeed", locale = "ru", widthDp = 960, heightDp = 540)
@Preview(name = "TvFeed EN", locale = "en", widthDp = 960, heightDp = 540)
@Composable
private fun TvFeedPreview(@PreviewParameter(TvFeedPreviewProvider::class) state: TvFeedState) {
    QuizzTvPreviewScreen {
        TvFeedStateContent(state, {}, false, Modifier.fillMaxSize(), {})
    }
}
