package com.quizzgame.shared.feed.domain.entity

import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo

data class Feed(
    val sections: List<FeedSection>,
    val page: CursorPageInfo = CursorPageInfo(false, null),
)
