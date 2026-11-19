package com.quizzgame.shared.feed.data.paging

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.data.FeedRemoteDataSource
import com.quizzgame.shared.feed.domain.entity.FeedResult
import com.quizzgame.shared.feed.domain.entity.section.QuizRail

class RailItemsPagingSource(
    private val rail: QuizRail,
    private val remote: FeedRemoteDataSource,
    private val owner: SessionState,
    onExpired: () -> Boolean = { false },
    private val isCurrent: () -> Boolean = { true },
) : CursorPagingSource<QuizCard>(QuizCard::id, onExpired) {
    override suspend fun request(cursor: String?): FeedResult<CursorBatch<QuizCard>> {
        if (!isCurrent()) return FeedResult.Failed(AppFailure.OperationInterrupted)
        val result = if (cursor == null) {
            FeedResult.Loaded(CursorBatch(rail.cards, rail.itemsPage))
        } else {
            remote.loadItems(rail.id, cursor, owner).mapContent { CursorBatch(it.cards, it.page) }
        }
        return if (isCurrent()) result else FeedResult.Failed(AppFailure.OperationInterrupted)
    }
}
