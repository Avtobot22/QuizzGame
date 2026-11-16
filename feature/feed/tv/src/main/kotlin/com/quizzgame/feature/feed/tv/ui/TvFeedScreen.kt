package com.quizzgame.feature.feed.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import com.quizzgame.feature.feed.tv.presentation.TvFeedViewModel
import com.quizzgame.feature.feed.tv.presentation.feedSections
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

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
    val scope = rememberCoroutineScope()
    val pages = remember(viewModel, scope) {
        viewModel.state.feedSections().stateIn(
            scope,
            SharingStarted.Eagerly,
            (viewModel.state.value as? TvFeedState.Content)?.sections ?: PagingData.empty(),
        )
    }
    val sections = pages.collectAsLazyPagingItems()
    val refresh = sections.loadState.refresh
    when {
        sections.itemCount > 0 && content != null ->
            TvPagedFeedContent(sections, viewModel, content, requestFocus, modifier, onSignIn)
        refresh is LoadState.Error -> TvFeedPageError(
            refresh.error,
            false,
            sections::retry,
            onSignIn,
            modifier,
            OperationKind.INITIAL_LOAD,
        )
        refresh is LoadState.Loading -> TvFeedLoading()
        else -> TvFeedEmpty(sections::refresh, requestFocus)
    }
}
