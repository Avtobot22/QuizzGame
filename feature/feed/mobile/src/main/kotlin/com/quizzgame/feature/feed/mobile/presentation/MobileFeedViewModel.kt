package com.quizzgame.feature.feed.mobile.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.feed.domain.LoadFeedUseCase
import com.quizzgame.shared.feed.domain.LoadRailItemsUseCase
import com.quizzgame.shared.feed.domain.entity.QuizRail
import com.quizzgame.shared.feed.presentation.FeedRailCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileFeedViewModel @Inject constructor(
    loadFeed: LoadFeedUseCase,
    loadItems: LoadRailItemsUseCase,
) : BaseViewModel<MobileFeedState, MobileFeedIntent, MobileFeedEvent>() {
    val feed = loadFeed().cachedIn(viewModelScope)
    private val rails = FeedRailCache(viewModelScope, loadItems::invoke)
    override fun createInitialState() = MobileFeedState()
    fun items(rail: QuizRail) = rails.items(rail)
    override fun handleIntent(intent: MobileFeedIntent) {
        when (intent) {
            MobileFeedIntent.Refresh -> viewModelScope.launch { emitEvent(MobileFeedEvent.REFRESH) }
            MobileFeedIntent.Retry -> viewModelScope.launch { emitEvent(MobileFeedEvent.RETRY) }
            is MobileFeedIntent.Open -> updateState {
                if (it.destination == null) it.copy(destination = intent.entryId) else it
            }
            MobileFeedIntent.NavigationHandled -> updateState { it.copy(destination = null) }
        }
    }
}
