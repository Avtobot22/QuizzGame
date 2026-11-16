package com.quizzgame.feature.feed.tv.presentation

import androidx.paging.PagingData
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

internal sealed interface TvFeedState {
    data object Loading : TvFeedState
    data class Content(
        val value: Feed,
        val sections: PagingData<QuizRail> = PagingData.from(value.rails),
        val prefetch: com.quizzgame.core.flags.CatalogPrefetch = com.quizzgame.core.flags.CatalogPrefetch(),
        val parallax: Boolean = false,
        val refreshing: Boolean = false,
        val appendingSections: Boolean = false,
        val appendingRails: Set<String> = emptySet(),
        val destination: String? = null,
        val error: ErrorPresentationSpec? = null,
    ) : TvFeedState
    data object Empty : TvFeedState
    data class Error(val spec: ErrorPresentationSpec) : TvFeedState
}

internal fun Flow<TvFeedState>.feedSections(): Flow<PagingData<QuizRail>> =
    mapNotNull { (it as? TvFeedState.Content)?.sections }.distinctUntilChanged()
