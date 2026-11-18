package com.quizzgame.shared.feed.data

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.feed.domain.entity.Feed
import com.quizzgame.shared.feed.domain.entity.FeedResult
import com.quizzgame.shared.feed.domain.entity.RailItems

interface FeedRemoteDataSource {
    val sectionPageSize: Int get() = 5
    val itemPageSize: Int get() = 12
    suspend fun load(cursor: String? = null, owner: SessionState? = null): FeedResult<Feed>
    suspend fun loadItems(sectionId: String, cursor: String, owner: SessionState): FeedResult<RailItems>
}
