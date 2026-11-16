package com.quizzgame.feature.feed.tv.domain

import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

internal class LoadFeedUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke(scope: CoroutineScope) = repository.load(scope)
}
