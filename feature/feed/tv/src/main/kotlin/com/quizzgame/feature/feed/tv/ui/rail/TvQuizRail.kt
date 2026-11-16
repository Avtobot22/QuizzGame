package com.quizzgame.feature.feed.tv.ui.rail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import com.quizzgame.feature.feed.tv.ui.card.TvRailCard
import com.quizzgame.feature.feed.tv.ui.status.TvFeedPageError
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags

@Composable
internal fun TvQuizRail(
    rail: QuizRail,
    onOpen: (String) -> Unit,
    parallax: Boolean,
    appending: Boolean = false,
    entryFocus: FocusRequester? = null,
    headerAction: (@Composable () -> Unit)? = null,
    pagingItems: LazyPagingItems<QuizCard>? = null,
    onSignIn: () -> Unit = {},
    restoreCardId: String? = null,
    onFocused: (String) -> Unit = {},
) {
    val focus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val open: (String) -> Unit = {
        focus.saveFocusedChild()
        onOpen(it)
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TvQuizRailHeader(rail, headerAction)
        LazyRow(
            state = listState,
            modifier = Modifier.focusRequester(focus)
                .then(if (entryFocus != null) Modifier.focusRequester(entryFocus) else Modifier).focusRestorer(),
            contentPadding = PaddingValues(
                horizontal = QuizzTvLayout.screenInset.dp,
                vertical = QuizzTvLayout.focusInset.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp),
        ) {
            if (pagingItems != null) {
                items(pagingItems.itemCount, key = pagingItems.itemKey { it.id }) { index ->
                    pagingItems[index]?.let { card ->
                        TvRailCard(card, open, parallax, onFocused, card.id == restoreCardId)
                    }
                }
            } else {
                items(rail.cards, key = { it.id }) { card -> TvRailCard(card, open, parallax) }
            }
            val error = (pagingItems?.loadState?.append as? LoadState.Error)
                ?: (pagingItems?.loadState?.refresh as? LoadState.Error)
            if (error != null && pagingItems != null) {
                item(key = "cards-error") {
                    TvFeedPageError(
                        error.error,
                        true,
                        pagingItems::retry,
                        onSignIn,
                        Modifier.width(QuizzTvLayout.cardWidth.dp),
                        tag = TvFeedTags.retryRail(rail.id),
                    )
                }
            }
            if (appending || pagingItems?.loadState?.append is LoadState.Loading) {
                item(key = "cards-loading") { TvRailAppending(rail.id) }
            }
        }
    }
}
