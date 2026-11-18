package com.quizzgame.shared.feed.domain

import com.quizzgame.shared.feed.domain.entity.QuizRail
import javax.inject.Inject

class LoadRailItemsUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke(rail: QuizRail) = repository.loadItems(rail)
}
