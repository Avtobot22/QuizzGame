package com.quizzgame.feature.feed.tv.data.paging

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSource
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail

internal class FeedSectionsPagingSource(
    private val remote: FeedRemoteDataSource,
    private val owner: SessionState,
    private val beginSnapshot: () -> Long = { 0L },
    onExpired: () -> Boolean = { false },
) : CursorPagingSource<QuizRail>(QuizRail::id, onExpired) {
    private var snapshotId = 0L

    override suspend fun request(cursor: String?): FeedResult<CursorBatch<QuizRail>> =
        remote.load(cursor, owner).mapContent { feed ->
            if (cursor == null && !invalid) snapshotId = beginSnapshot()
            CursorBatch(feed.rails.map { it.copy(snapshotId = snapshotId) }, feed.page)
        }
}
