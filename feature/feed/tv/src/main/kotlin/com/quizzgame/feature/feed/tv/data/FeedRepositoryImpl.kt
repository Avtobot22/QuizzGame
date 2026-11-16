package com.quizzgame.feature.feed.tv.data

import androidx.paging.PagingData
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.feature.feed.tv.data.paging.FeedPagingSession
import com.quizzgame.feature.feed.tv.domain.FeedRepository
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

internal class FeedRepositoryImpl @Inject constructor(
    private val remote: FeedRemoteDataSource,
    private val session: SessionStateReader,
) : FeedRepository {
    private var paging: FeedPagingSession? = null

    override fun load(scope: CoroutineScope): Flow<PagingData<QuizRail>> = flow {
        val owner = session.current()
        emitAll(createPaging(owner, scope).sections)
    }

    @Synchronized
    override fun loadItems(rail: QuizRail): Flow<PagingData<QuizCard>> =
        paging?.items(rail) ?: flowOf(PagingData.empty())

    @Synchronized
    private fun createPaging(owner: SessionState, scope: CoroutineScope): FeedPagingSession {
        paging?.close()
        return FeedPagingSession(remote, owner, scope).also { paging = it }
    }
}
