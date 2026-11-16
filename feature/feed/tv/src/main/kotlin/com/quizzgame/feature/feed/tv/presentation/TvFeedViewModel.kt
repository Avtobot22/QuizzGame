package com.quizzgame.feature.feed.tv.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.feed.tv.domain.LoadFeedUseCase
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvFeedViewModel @Inject constructor(
    private val loadFeedUseCase: LoadFeedUseCase,
    private val flags: com.quizzgame.core.flags.AppFeatureFlags,
) : BaseViewModel<TvFeedState, TvFeedIntent, Nothing>() {
    private var loading: Job? = null

    init { load() }

    override fun createInitialState(): TvFeedState = TvFeedState.Loading

    override fun handleIntent(intent: TvFeedIntent) {
        when (intent) {
            TvFeedIntent.Retry -> load()
            TvFeedIntent.Refresh -> load()
            is TvFeedIntent.Open -> {
                val content = (state.value as? TvFeedState.Content)?.value
                if ((state.value as? TvFeedState.Content)?.destination == null && content?.rails?.any { rail ->
                        rail.cards.any { it.entryId == intent.entryId }
                    } == true
                ) {
                    updateState { (it as TvFeedState.Content).copy(destination = intent.entryId) }
                }
            }
            TvFeedIntent.DismissError -> updateState { (it as? TvFeedState.Content)?.copy(error = null) ?: it }
            TvFeedIntent.NavigationHandled -> updateState {
                (it as? TvFeedState.Content)?.copy(destination = null) ?: it
            }
        }
    }

    private fun load() {
        if (loading?.isActive == true) return
        val previous = state.value as? TvFeedState.Content
        updateState { previous?.copy(refreshing = true, error = null) ?: TvFeedState.Loading }
        loading = viewModelScope.launch {
            val next = when (val result = loadFeedUseCase()) {
                is FeedResult.Loaded -> if (result.content.rails.isEmpty()) {
                    TvFeedState.Empty
                } else {
                    TvFeedState.Content(
                        result.content,
                        prefetch = flags.catalogPrefetch(),
                        parallax = flags.catalogParallax(),
                    )
                }
                is FeedResult.Failed -> {
                    val spec = TvErrorDecisionPolicy().decide(
                        result.failure,
                        ErrorOperationContext(
                            if (previous == null) OperationKind.INITIAL_LOAD else OperationKind.USER_REFRESH,
                            previous != null,
                            RetrySafety.SAFE_TO_REPEAT,
                        ),
                    )
                    previous?.copy(refreshing = false, error = spec) ?: TvFeedState.Error(spec)
                }
            }
            updateState { next }
        }
    }
}
