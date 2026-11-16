package com.quizzgame.feature.feed.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import com.quizzgame.feature.feed.tv.presentation.TvFeedViewModel

@Composable
fun TvFeedScreen(
    onOpenEntry: (String) -> Boolean,
    requestFocus: Boolean,
    modifier: Modifier = Modifier,
    onSignIn: () -> Unit = {
    },
) {
    val viewModel: TvFeedViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val content = state as? TvFeedState.Content
    NavigationResultEffect(content?.destination, { viewModel.onIntent(TvFeedIntent.NavigationHandled) }, onOpenEntry)
    TvFeedStateContent(state, viewModel::onIntent, requestFocus, modifier, onSignIn)
}
