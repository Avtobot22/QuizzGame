package com.quizzgame.shared.feed.domain.entity

import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo
import com.quizzgame.shared.catalog.domain.entity.QuizCard

data class QuizRail(
    override val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val cards: List<QuizCard>,
    val itemsPage: CursorPageInfo = CursorPageInfo(false, null),
    val snapshotId: Long = 0,
) : FeedSection
