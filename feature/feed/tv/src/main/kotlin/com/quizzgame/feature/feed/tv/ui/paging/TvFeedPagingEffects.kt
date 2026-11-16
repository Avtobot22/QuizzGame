package com.quizzgame.feature.feed.tv.ui.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.compose.LazyPagingItems
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import com.quizzgame.feature.feed.tv.presentation.TvFeedEvent
import kotlinx.coroutines.flow.Flow

@Composable
internal fun TvFeedPagingEffects(events: Flow<TvFeedEvent>, sections: LazyPagingItems<QuizRail>) {
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(events, sections, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            events.collect { event ->
                when (event) {
                    TvFeedEvent.REFRESH -> sections.refresh()
                    TvFeedEvent.RETRY -> sections.retry()
                }
            }
        }
    }
}
