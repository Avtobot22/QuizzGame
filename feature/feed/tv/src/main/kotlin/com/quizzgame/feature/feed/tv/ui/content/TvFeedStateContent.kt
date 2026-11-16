package com.quizzgame.feature.feed.tv.ui.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import com.quizzgame.feature.feed.tv.ui.status.TvFeedEmpty
import com.quizzgame.feature.feed.tv.ui.status.TvFeedError
import com.quizzgame.feature.feed.tv.ui.status.TvFeedLoading

@Composable
internal fun TvFeedStateContent(
    state: TvFeedState,
    onIntent: (TvFeedIntent) -> Unit,
    requestFocus: Boolean,
    modifier: Modifier,
    onSignIn: () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        when (state) {
            TvFeedState.Loading -> TvFeedLoading()
            TvFeedState.Empty -> TvFeedEmpty({ onIntent(TvFeedIntent.Retry) }, requestFocus)
            is TvFeedState.Error -> TvFeedError(state.spec, { onIntent(TvFeedIntent.Retry) }, onSignIn)
            is TvFeedState.Content -> TvFeedContent(state, onIntent, requestFocus, Modifier.fillMaxSize(), onSignIn)
        }
    }
}
