package com.quizzgame.feature.feed.tv.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.feed.domain.LoadFeedUseCase
import com.quizzgame.shared.feed.domain.LoadRailItemsUseCase
import com.quizzgame.shared.feed.domain.entity.QuizRail
import com.quizzgame.shared.feed.presentation.FeedRailCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvFeedViewModel @Inject constructor(
    loadFeedUseCase: LoadFeedUseCase,
    loadRailItemsUseCase: LoadRailItemsUseCase,
    private val flags: AppFeatureFlags,
) : BaseViewModel<TvFeedState, TvFeedIntent, TvFeedEvent>() {
    val feed = loadFeedUseCase().map { pages ->
        pages.filter { it is QuizRail }.map { it as QuizRail }
    }.cachedIn(viewModelScope)
    private val rails = FeedRailCache(viewModelScope, loadRailItemsUseCase::invoke)

    override fun createInitialState() = TvFeedState(
        prefetch = flags.catalogPrefetch(),
        parallax = flags.catalogParallax(),
    )

    fun items(rail: QuizRail) = rails.items(rail)

    override fun handleIntent(intent: TvFeedIntent) {
        when (intent) {
            TvFeedIntent.Retry -> viewModelScope.launch { emitEvent(TvFeedEvent.RETRY) }
            TvFeedIntent.Refresh -> viewModelScope.launch { emitEvent(TvFeedEvent.REFRESH) }
            is TvFeedIntent.Open -> updateState {
                if (it.destination == null && intent.entryId.isNotBlank()) it.copy(destination = intent.entryId) else it
            }
            TvFeedIntent.NavigationHandled -> updateState { it.copy(destination = null) }
        }
    }
}
