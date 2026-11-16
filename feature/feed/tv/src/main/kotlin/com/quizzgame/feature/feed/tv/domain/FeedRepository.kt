package com.quizzgame.feature.feed.tv.domain

import com.quizzgame.feature.feed.tv.domain.entity.FeedResult

internal interface FeedRepository {
    suspend fun load(): FeedResult
}
