package com.quizzgame.feature.feed.mobile.ui.tags

object MobileFeedTags {
    const val ROOT = "mobile-feed"
    const val LIST = "mobile-feed-list"
    const val FEATURED_HEADING = "mobile-feed-featured-heading"
    const val REFRESH = "mobile-feed-refresh"
    const val LOADING = "mobile-feed-loading"
    const val APPENDING_SECTIONS = "mobile-feed-appending-sections"
    const val REFRESH_ERROR = "mobile-feed-refresh-error"
    const val APPEND_ERROR = "mobile-feed-append-error"
    const val EMPTY = "mobile-feed-empty"
    const val ERROR = "mobile-feed-error"
    fun section(id: String) = "mobile-feed-section-$id"
    fun row(id: String) = "mobile-feed-row-$id"
    fun appendingRail(id: String) = "mobile-feed-appending-$id"
    fun retryRail(id: String) = "mobile-feed-retry-$id"
    fun card(id: String) = "mobile-feed-card-$id"
    fun artwork(id: String) = "mobile-feed-artwork-$id"
}
