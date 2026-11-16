package com.quizzgame.feature.feed.tv.domain.entity

internal data class RailItems(val sectionId: String, val cards: List<QuizCard>, val page: CursorPageInfo)
