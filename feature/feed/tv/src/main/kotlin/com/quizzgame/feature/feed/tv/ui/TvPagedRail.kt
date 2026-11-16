package com.quizzgame.feature.feed.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import com.quizzgame.feature.feed.tv.presentation.TvFeedState
import kotlinx.coroutines.flow.Flow

@Composable
internal fun TvPagedRail(
    rail: QuizRail,
    items: (QuizRail) -> Flow<PagingData<QuizCard>>,
    state: TvFeedState,
    entryFocus: FocusRequester?,
    headerAction: (@Composable () -> Unit)?,
    onOpen: (String) -> Unit,
    onSignIn: () -> Unit,
    restoreCardId: String? = null,
    onFocused: (String) -> Unit = {},
) {
    val flow = remember(items, rail.id, rail.snapshotId) { items(rail) }
    val cards = flow.collectAsLazyPagingItems()
    TvRailImagePrefetch(cards, state.prefetch)
    TvQuizRail(
        rail, onOpen, state.parallax,
        entryFocus = entryFocus,
        headerAction = headerAction,
        pagingItems = cards,
        onSignIn = onSignIn,
        restoreCardId = restoreCardId,
        onFocused = onFocused,
    )
}
