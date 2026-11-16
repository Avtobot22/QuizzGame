package com.quizzgame.feature.feed.tv.domain

import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import javax.inject.Inject

internal class LoadFeedUseCase @Inject constructor(
    private val repository: FeedRepository,
) : suspend () -> FeedResult by repository::load
