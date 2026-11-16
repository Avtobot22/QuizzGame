package com.quizzgame.feature.feed.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
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
    val sections = viewModel.feed.collectAsLazyPagingItems()
    TvFeedPagingEffects(viewModel.events, sections)
    NavigationResultEffect(state.destination, { viewModel.onIntent(TvFeedIntent.NavigationHandled) }, onOpenEntry)
    val refresh = sections.loadState.refresh
    Box(modifier.fillMaxSize()) {
        when {
            sections.itemCount > 0 -> TvPagedFeedContent(
                sections,
                viewModel::onIntent,
                viewModel::items,
                state,
                requestFocus,
                Modifier,
                onSignIn,
            )
            refresh is LoadState.Error -> TvFeedPageError(
                refresh.error,
                false,
                { viewModel.onIntent(TvFeedIntent.Retry) },
                onSignIn,
                operation = OperationKind.INITIAL_LOAD,
            )
            refresh is LoadState.Loading -> TvFeedLoading()
            else -> TvFeedEmpty({ viewModel.onIntent(TvFeedIntent.Refresh) }, requestFocus)
        }
    }
}
