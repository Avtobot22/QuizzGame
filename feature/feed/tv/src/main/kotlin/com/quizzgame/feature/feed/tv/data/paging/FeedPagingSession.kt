package com.quizzgame.feature.feed.tv.data.paging

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSource
import com.quizzgame.feature.feed.tv.data.api.TvFeedApi
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicLong

/** Связывает снимок секций и его ряды; не запускает корутины и не кеширует страницы. */
internal class FeedPagingSession(
    private val remote: FeedRemoteDataSource,
    private val owner: SessionState,
) {
    private var recoveryUsed = false
    private var automaticRestart = false
    private var snapshotId = 0L
    private var closed = false
    private var source: FeedSectionsPagingSource? = null
    private val itemSources = mutableSetOf<RailItemsPagingSource>()

    val sections: Flow<PagingData<QuizRail>> = Pager(config(TvFeedApi.SECTION_LIMIT)) {
        createSource()
    }.flow

    @Synchronized
    fun owns(rail: QuizRail): Boolean = !closed && snapshotId != 0L && rail.snapshotId == snapshotId

    fun items(rail: QuizRail): Flow<PagingData<QuizCard>> = flow {
        if (!owns(rail)) {
            emit(PagingData.empty(sourceLoadStates = EmptyLoadStates))
        } else {
            var source: RailItemsPagingSource? = null
            try {
                emitAll(Pager(config(TvFeedApi.ITEM_LIMIT)) { createItemSource(rail).also { source = it } }.flow)
            } finally {
                source?.invalidate()
            }
        }
    }

    @Synchronized
    fun close() {
        closed = true
        source?.invalidate()
        invalidateItems()
    }

    @Synchronized
    private fun createSource(): FeedSectionsPagingSource {
        if (!automaticRestart) recoveryUsed = false
        automaticRestart = false
        return FeedSectionsPagingSource(remote, owner, ::beginSnapshot, ::expire).also { source = it }
    }

    @Synchronized
    private fun createItemSource(rail: QuizRail): RailItemsPagingSource =
        RailItemsPagingSource(rail, remote, owner, { expire(rail) }, { owns(rail) }).also { source ->
            itemSources.add(source)
            source.registerInvalidatedCallback { synchronized(this) { itemSources.remove(source) } }
        }

    @Synchronized
    private fun beginSnapshot(): Long {
        snapshotId = SnapshotSequence.incrementAndGet()
        invalidateItems()
        return snapshotId
    }

    private fun invalidateItems() {
        val previous = itemSources.toList()
        itemSources.clear()
        previous.forEach { it.invalidate() }
    }

    @Synchronized
    private fun expire(rail: QuizRail): Boolean = owns(rail) && expire()

    @Synchronized
    private fun expire(): Boolean {
        if (closed || recoveryUsed) return false
        recoveryUsed = true
        automaticRestart = true
        source?.invalidate()
        return true
    }
}

private fun config(size: Int) = PagingConfig(
    pageSize = size,
    initialLoadSize = size,
    prefetchDistance = 2,
    enablePlaceholders = false,
)

private val SnapshotSequence = AtomicLong()

private val EmptyLoadStates = LoadStates(
    refresh = LoadState.NotLoading(true),
    prepend = LoadState.NotLoading(true),
    append = LoadState.NotLoading(true),
)
