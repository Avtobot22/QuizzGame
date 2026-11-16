package com.quizzgame.feature.feed.tv.domain

import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import javax.inject.Inject

internal class LoadRailItemsUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke(rail: QuizRail) = repository.loadItems(rail)
}
