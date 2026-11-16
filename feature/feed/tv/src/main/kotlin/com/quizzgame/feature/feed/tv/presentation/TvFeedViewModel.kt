package com.quizzgame.feature.feed.tv.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.feed.tv.domain.LoadFeedUseCase
import com.quizzgame.feature.feed.tv.domain.LoadRailItemsUseCase
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvFeedViewModel @Inject constructor(
    loadFeedUseCase: LoadFeedUseCase,
    loadRailItemsUseCase: LoadRailItemsUseCase,
    private val flags: AppFeatureFlags,
) : BaseViewModel<TvFeedState, TvFeedIntent, TvFeedEvent>() {
    val feed = loadFeedUseCase().cachedIn(viewModelScope)
    private val rails = TvFeedRailCache(viewModelScope, loadRailItemsUseCase::invoke)

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
