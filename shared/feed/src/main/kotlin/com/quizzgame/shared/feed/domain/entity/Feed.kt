package com.quizzgame.shared.feed.domain.entity

import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo
import com.quizzgame.shared.feed.domain.entity.section.FeedSection

data class Feed(
    val sections: List<FeedSection>,
    val page: CursorPageInfo = CursorPageInfo(false, null),
)
