package com.quizzgame.feature.feed.tv.data.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSource
import com.quizzgame.feature.feed.tv.data.api.TvFeedApi
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import java.util.concurrent.atomic.AtomicLong

internal class FeedPagingSession(
    private val remote: FeedRemoteDataSource,
    private val owner: SessionState,
    parentScope: CoroutineScope,
) {

    private val scope = CoroutineScope(parentScope.coroutineContext + Job(parentScope.coroutineContext[Job]))
    private var recoveryUsed = false
    private var automaticRestart = false
    private var snapshotId = 0L
    private var source: FeedSectionsPagingSource? = null
    private val rails = LinkedHashMap<String, RailPager>(MAX_RETAINED_RAILS, 0.75f, true)

    val sections = Pager(config(TvFeedApi.SECTION_LIMIT)) {
        if (!automaticRestart) recoveryUsed = false
        automaticRestart = false
        FeedSectionsPagingSource(remote, owner, ::beginSnapshot, ::expire).also { source = it }
    }.flow.cachedIn(scope)

    @Synchronized
    fun items(rail: QuizRail): Flow<PagingData<QuizCard>> {
        if (!scope.isActive || rail.snapshotId != snapshotId) return flowOf(PagingData.empty())
        return rails.getOrPut(rail.id) {
            val job = Job(scope.coroutineContext[Job])
            val railScope = CoroutineScope(scope.coroutineContext + job)
            val pages = Pager(config(TvFeedApi.ITEM_LIMIT)) {
                RailItemsPagingSource(rail, remote, owner, ::expire)
            }.flow.cachedIn(railScope)
            RailPager(
                pages.onStart { retain(rail) }.onCompletion { release(rail) }
                    .shareIn(railScope, SharingStarted.WhileSubscribed(), replay = 1),
                job,
            )
        }.flow
    }

    @Synchronized
    private fun retain(rail: QuizRail) {
        if (rail.snapshotId == snapshotId) rails[rail.id]?.let { it.references++ }
    }

    @Synchronized
    private fun release(rail: QuizRail) {
        if (rail.snapshotId != snapshotId) return
        rails[rail.id]?.let { it.references = (it.references - 1).coerceAtLeast(0) }
        val inactive = rails.entries.filter { it.value.references == 0 }
        inactive.take((rails.size - MAX_RETAINED_RAILS).coerceAtLeast(0)).forEach {
            it.value.job.cancel()
            rails.remove(it.key)
        }
    }

    fun close() {
        scope.cancel()
    }

    @Synchronized
    private fun beginSnapshot(): Long {
        rails.values.forEach { it.job.cancel() }
        rails.clear()
        snapshotId = SnapshotSequence.incrementAndGet()
        return snapshotId
    }

    @Synchronized
    private fun expire(): Boolean {
        if (recoveryUsed) return false
        recoveryUsed = true
        automaticRestart = true
        source?.invalidate()
        return true
    }

    private class RailPager(val flow: Flow<PagingData<QuizCard>>, val job: Job, var references: Int = 0)
}

private fun config(size: Int) = PagingConfig(
    pageSize = size,
    initialLoadSize = size,
    prefetchDistance = 2,
    enablePlaceholders = false,
)

private const val MAX_RETAINED_RAILS = 12

private val SnapshotSequence = AtomicLong()
