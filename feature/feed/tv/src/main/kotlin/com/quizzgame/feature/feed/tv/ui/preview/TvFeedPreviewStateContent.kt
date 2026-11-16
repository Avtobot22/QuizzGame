package com.quizzgame.feature.feed.tv.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
import com.quizzgame.feature.feed.tv.ui.status.TvFeedEmpty
import com.quizzgame.feature.feed.tv.ui.status.TvFeedError
import com.quizzgame.feature.feed.tv.ui.status.TvFeedLoading

@Composable
internal fun TvFeedPreviewStateContent(
    state: TvFeedPreviewState,
    onIntent: (TvFeedIntent) -> Unit,
    requestFocus: Boolean,
    modifier: Modifier,
    onSignIn: () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        when (state) {
            TvFeedPreviewState.Loading -> TvFeedLoading()
            TvFeedPreviewState.Empty -> TvFeedEmpty({ onIntent(TvFeedIntent.Retry) }, requestFocus)
            is TvFeedPreviewState.Error -> TvFeedError(state.spec, { onIntent(TvFeedIntent.Retry) }, onSignIn)
            is TvFeedPreviewState.Content -> TvFeedPreviewContent(
                state,
                onIntent,
                requestFocus,
                Modifier.fillMaxSize(),
                onSignIn,
            )
        }
    }
}
