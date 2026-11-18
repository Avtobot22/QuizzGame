package com.quizzgame.shared.feed.domain.entity

import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo
import com.quizzgame.shared.catalog.domain.entity.QuizCard

data class RailItems(val sectionId: String, val cards: List<QuizCard>, val page: CursorPageInfo)
