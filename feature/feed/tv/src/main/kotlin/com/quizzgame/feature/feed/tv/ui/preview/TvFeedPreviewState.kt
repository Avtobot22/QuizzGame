package com.quizzgame.feature.feed.tv.ui.preview

import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.flags.CatalogPrefetch
import com.quizzgame.shared.feed.domain.entity.Feed

internal sealed interface TvFeedPreviewState {
    data object Loading : TvFeedPreviewState
    data object Empty : TvFeedPreviewState
    data class Error(val spec: ErrorPresentationSpec) : TvFeedPreviewState
    data class Content(
        val value: Feed,
        val prefetch: CatalogPrefetch = CatalogPrefetch(),
        val parallax: Boolean = false,
        val refreshing: Boolean = false,
        val appendingSections: Boolean = false,
        val appendingRails: Set<String> = emptySet(),
        val error: ErrorPresentationSpec? = null,
    ) : TvFeedPreviewState
}
