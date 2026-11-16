package com.quizzgame.feature.feed.tv.data.paging

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSource
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail

internal class RailItemsPagingSource(
    private val rail: QuizRail,
    private val remote: FeedRemoteDataSource,
    private val owner: SessionState,
    onExpired: () -> Boolean = { false },
) : CursorPagingSource<QuizCard>(QuizCard::id, onExpired) {
    override suspend fun request(cursor: String?): FeedResult<CursorBatch<QuizCard>> = if (cursor == null) {
        FeedResult.Loaded(CursorBatch(rail.cards, rail.itemsPage))
    } else {
        remote.loadItems(rail.id, cursor, owner).mapContent { CursorBatch(it.cards, it.page) }
    }
}
