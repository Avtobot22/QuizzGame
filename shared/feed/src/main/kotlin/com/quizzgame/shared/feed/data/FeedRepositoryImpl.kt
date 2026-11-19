package com.quizzgame.shared.feed.data

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.catalog.domain.entity.QuizCard
import com.quizzgame.shared.feed.data.paging.FeedPagingSession
import com.quizzgame.shared.feed.domain.entity.section.FeedSection
import com.quizzgame.shared.feed.domain.entity.section.QuizRail
import com.quizzgame.shared.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val remote: FeedRemoteDataSource,
    private val session: SessionStateReader,
) : FeedRepository {
    private val sessions = mutableSetOf<FeedPagingSession>()

    override fun getFeed(): Flow<PagingData<FeedSection>> = flow {
        val paging = FeedPagingSession(remote, session.current())
        synchronized(sessions) { sessions.add(paging) }
        try {
            emitAll(paging.sections)
        } finally {
            synchronized(sessions) { sessions.remove(paging) }
            paging.close()
        }
    }

    override fun loadItems(rail: QuizRail): Flow<PagingData<QuizCard>> = flow {
        val paging = synchronized(sessions) { sessions.firstOrNull { it.owns(rail) } }
        if (paging == null) emit(PagingData.empty(sourceLoadStates = EmptyLoadStates)) else emitAll(paging.items(rail))
    }
}

private val EmptyLoadStates = LoadStates(
    refresh = LoadState.NotLoading(true),
    prepend = LoadState.NotLoading(true),
    append = LoadState.NotLoading(true),
)
