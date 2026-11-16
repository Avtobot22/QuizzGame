package com.quizzgame.feature.feed.tv.ui.tags

object TvFeedTags {
    fun card(id: String): String = "feed:card:$id"
    fun appendingRail(id: String): String = "feed:appending:$id"
    const val APPENDING_SECTIONS = "feed:appending-sections"
    const val CONTENT = "feed:content"
    const val LOADING = "feed:loading"
    const val EMPTY = "feed:empty"
    fun retryRail(id: String): String = "feed:retry-rail:$id"
    const val APPEND_RETRY = "feed:retry-sections"
    const val REFRESH_RETRY = "feed:retry-refresh"
    const val RETRY = "feed:retry"
    const val REFRESH = "feed:refresh"
}
