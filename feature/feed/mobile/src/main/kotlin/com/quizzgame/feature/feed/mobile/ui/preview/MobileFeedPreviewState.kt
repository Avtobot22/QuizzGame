package com.quizzgame.feature.feed.mobile.ui.preview

import androidx.paging.LoadState
import com.quizzgame.shared.feed.domain.entity.Feed

internal sealed interface MobileFeedPreviewState {
    data object Loading : MobileFeedPreviewState
    data object Empty : MobileFeedPreviewState
    data class Error(val cause: Throwable) : MobileFeedPreviewState
    data class Content(
        val value: Feed,
        val refresh: LoadState = LoadState.NotLoading(false),
        val append: LoadState = LoadState.NotLoading(true),
        val railAppend: LoadState = LoadState.NotLoading(true),
    ) : MobileFeedPreviewState
}
