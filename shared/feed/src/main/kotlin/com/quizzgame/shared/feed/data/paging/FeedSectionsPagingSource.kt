package com.quizzgame.shared.feed.data.paging

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.feed.data.FeedRemoteDataSource
import com.quizzgame.shared.feed.domain.entity.FeedResult
import com.quizzgame.shared.feed.domain.entity.FeedSection
import com.quizzgame.shared.feed.domain.entity.QuizRail

class FeedSectionsPagingSource(
    private val remote: FeedRemoteDataSource,
    private val owner: SessionState,
    private val beginSnapshot: () -> Long = { 0L },
    onExpired: () -> Boolean = { false },
) : CursorPagingSource<FeedSection>(FeedSection::id, onExpired) {
    private var snapshotId = 0L

    override suspend fun request(cursor: String?): FeedResult<CursorBatch<FeedSection>> =
        remote.load(cursor, owner).mapContent { feed ->
            if (snapshotId == 0L && cursor == null && !invalid) snapshotId = beginSnapshot()
            CursorBatch(feed.sections.map { if (it is QuizRail) it.copy(snapshotId = snapshotId) else it }, feed.page)
        }
}
