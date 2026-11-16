package com.quizzgame.feature.feed.tv.domain.entity

import com.quizzgame.core.error.failure.AppFailure

internal sealed interface FeedResult {
    data class Loaded(val content: Feed) : FeedResult
    data class Failed(val failure: AppFailure) : FeedResult
}
