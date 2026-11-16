package com.quizzgame.feature.feed.tv.data

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.tv.domain.entity.Feed
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.RailItems

internal interface FeedRemoteDataSource {
    suspend fun load(cursor: String? = null, owner: SessionState? = null): FeedResult<Feed>
    suspend fun loadItems(sectionId: String, cursor: String, owner: SessionState): FeedResult<RailItems>
}
