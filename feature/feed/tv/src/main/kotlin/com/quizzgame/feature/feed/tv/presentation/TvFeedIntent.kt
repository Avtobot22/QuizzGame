package com.quizzgame.feature.feed.tv.presentation

internal sealed interface TvFeedIntent {
    data object Retry : TvFeedIntent
    data object DismissError : TvFeedIntent
    data object Refresh : TvFeedIntent
    data class Open(val entryId: String) : TvFeedIntent
    data object NavigationHandled : TvFeedIntent
}
