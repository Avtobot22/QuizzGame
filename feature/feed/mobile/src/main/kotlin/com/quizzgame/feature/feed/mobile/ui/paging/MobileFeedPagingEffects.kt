package com.quizzgame.feature.feed.mobile.ui.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.compose.LazyPagingItems
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedEvent
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import kotlinx.coroutines.flow.Flow

@Composable
internal fun MobileFeedPagingEffects(events: Flow<MobileFeedEvent>, sections: LazyPagingItems<FeedSection>) {
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(events, sections, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            events.collect { event ->
                when (event) {
                    MobileFeedEvent.REFRESH -> sections.refresh()
                    MobileFeedEvent.RETRY -> sections.retry()
                }
            }
        }
    }
}
