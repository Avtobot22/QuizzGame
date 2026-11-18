package com.quizzgame.feature.feed.tv.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.presentation.TvFeedIntent
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import com.quizzgame.feature.feed.tv.ui.paging.TvFeedRefreshAction
import com.quizzgame.feature.feed.tv.ui.rail.TvPagedRail
import com.quizzgame.feature.feed.tv.ui.status.TvFeedAppendingSections
import com.quizzgame.feature.feed.tv.ui.status.TvFeedPageError
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.domain.entity.QuizRail
import kotlinx.coroutines.flow.Flow

@Composable
internal fun TvPagedFeedContent(
    sections: LazyPagingItems<QuizRail>,
    onIntent: (TvFeedIntent) -> Unit,
    items: (QuizRail) -> Flow<PagingData<QuizCard>>,
    state: TvFeedState,
    requestFocus: Boolean,
    modifier: Modifier,
    onSignIn: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    val firstRailFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedCard by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag(TvFeedTags.CONTENT)
            .focusRequester(focus).focusRestorer(firstRailFocus),
        contentPadding = PaddingValues(top = 8.dp, bottom = QuizzTvLayout.verticalInset.dp),
        verticalArrangement = Arrangement.spacedBy(QuizzTvLayout.sectionGap.dp),
    ) {
        items(sections.itemCount, key = sections.itemKey { it.id }) { index ->
            sections[index]?.let { rail ->
                TvPagedRail(
                    rail, items, state, firstRailFocus.takeIf { index == firstVisibleIndex },
                    headerAction = if (index == 0) {
                        {
                            TvFeedRefreshAction(sections.loadState.refresh is LoadState.Loading) {
                                onIntent(TvFeedIntent.Refresh)
                            }
                        }
                    } else {
                        null
                    },
                    onOpen = {
                        focus.saveFocusedChild()
                        onIntent(TvFeedIntent.Open(it))
                    },
                    onSignIn = onSignIn,
                    restoreCardId = if (
                        requestFocus && focusedCard == null && index == firstVisibleIndex
                    ) {
                        rail.cards.firstOrNull()?.id
                    } else {
                        focusedCard.takeIf { focusedSection == rail.id && requestFocus }
                    },
                    onFocused = {
                        focusedSection = rail.id
                        focusedCard = it
                    },
                )
            }
        }
        feedLoadStates(sections, onSignIn) { onIntent(TvFeedIntent.Retry) }
    }
    TvEntryFocus(focus, requestFocus)
}

private fun LazyListScope.feedLoadStates(
    sections: LazyPagingItems<QuizRail>,
    onSignIn: () -> Unit,
    onRetry: () -> Unit,
) {
    val refresh = sections.loadState.refresh as? LoadState.Error
    if (refresh != null) {
        item(key = "refresh-error") {
            TvFeedPageError(
                refresh.error,
                true,
                onRetry,
                onSignIn,
                operation = OperationKind.USER_REFRESH,
                tag = TvFeedTags.REFRESH_RETRY,
            )
        }
    }
    when (val append = sections.loadState.append) {
        is LoadState.Loading -> item(key = "sections-loading") { TvFeedAppendingSections() }
        is LoadState.Error -> item(key = "sections-error") {
            TvFeedPageError(append.error, true, onRetry, onSignIn, tag = TvFeedTags.APPEND_RETRY)
        }
        is LoadState.NotLoading -> Unit
    }
}
