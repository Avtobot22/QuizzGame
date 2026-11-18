package com.quizzgame.feature.feed.mobile.presentation

internal sealed interface MobileFeedIntent {
    data object Retry : MobileFeedIntent
    data object Refresh : MobileFeedIntent
    data class Open(val entryId: String) : MobileFeedIntent
    data object NavigationHandled : MobileFeedIntent
}
