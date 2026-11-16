package com.quizzgame.feature.feed.tv.presentation

import com.quizzgame.core.flags.CatalogPrefetch

internal data class TvFeedState(
    val prefetch: CatalogPrefetch = CatalogPrefetch(),
    val parallax: Boolean = false,
    val destination: String? = null,
)
