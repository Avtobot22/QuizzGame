package com.quizzgame.feature.feed.tv.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.feed.tv.domain.LoadFeedUseCase
import com.quizzgame.feature.feed.tv.domain.LoadRailItemsUseCase
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvFeedViewModel @Inject constructor(
    private val loadFeedUseCase: LoadFeedUseCase,
    private val loadRailItemsUseCase: LoadRailItemsUseCase,
    private val flags: AppFeatureFlags,
) : BaseViewModel<TvFeedState, TvFeedIntent, Nothing>() {
    init {
        observeSections()
    }

    override fun createInitialState(): TvFeedState = initialContent()

    fun items(rail: QuizRail) = loadRailItemsUseCase(rail)

    override fun handleIntent(intent: TvFeedIntent) {
        when (intent) {
            TvFeedIntent.Retry, TvFeedIntent.Refresh -> Unit
            is TvFeedIntent.Open -> updateState {
                val content = it as? TvFeedState.Content
                if (content?.destination == null && intent.entryId.isNotBlank()) {
                    content?.copy(destination = intent.entryId) ?: it
                } else {
                    it
                }
            }
            TvFeedIntent.DismissError -> updateState { (it as? TvFeedState.Content)?.copy(error = null) ?: it }
            TvFeedIntent.NavigationHandled -> updateState {
                (it as? TvFeedState.Content)?.copy(destination = null) ?: it
            }
        }
    }

    private fun observeSections() {
        viewModelScope.launch {
            loadFeedUseCase(viewModelScope).cachedIn(viewModelScope).collect { pages ->
                updateState { (it as TvFeedState.Content).copy(sections = pages) }
            }
        }
    }

    private fun initialContent() = TvFeedState.Content(
        Feed(emptyList()),
        sections = PagingData.empty(
            sourceLoadStates = LoadStates(
                LoadState.Loading,
                LoadState.NotLoading(true),
                LoadState.NotLoading(true),
            ),
        ),
        prefetch = flags.catalogPrefetch(),
        parallax = flags.catalogParallax(),
    )
}
