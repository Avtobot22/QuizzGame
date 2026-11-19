package com.quizzgame.shared.feed.domain.usecase

import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import com.quizzgame.shared.feed.domain.repository.FeedRepository
import javax.inject.Inject

class LoadRailItemsUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke(rail: QuizRail) = repository.loadItems(rail)
}
