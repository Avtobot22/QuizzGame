package com.quizzgame.shared.feed.domain.entity.section

import com.quizzgame.shared.catalog.domain.entity.QuizCard

data class QuizHero(override val id: String, val card: QuizCard) : FeedSection
