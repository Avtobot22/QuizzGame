package com.quizzgame.feature.feed.tv.domain.entity

import com.quizzgame.core.error.failure.AppFailure

internal sealed interface FeedResult<out T> {
    data class Loaded<T>(val content: T) : FeedResult<T>
    data class Failed(val failure: AppFailure) : FeedResult<Nothing>
    data object Expired : FeedResult<Nothing>
}
