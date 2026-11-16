package com.quizzgame.feature.feed.tv.presentation

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quizzgame.feature.feed.tv.domain.entity.QuizCard
import com.quizzgame.feature.feed.tv.domain.entity.QuizRail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn

internal class TvFeedRailCache(
    private val parentScope: CoroutineScope,
    private val load: (QuizRail) -> Flow<PagingData<QuizCard>>,
) {

    private var snapshotId = 0L
    private val rails = LinkedHashMap<String, RailPager>(MAX_INACTIVE_RAILS, 0.75f, true)

    @Synchronized
    fun items(rail: QuizRail): Flow<PagingData<QuizCard>> {
        if (rail.snapshotId < snapshotId) return flowOf(PagingData.empty(sourceLoadStates = EmptyLoadStates))
        if (rail.snapshotId != snapshotId) {
            rails.values.forEach { it.job.cancel() }
            rails.clear()
            snapshotId = rail.snapshotId
        }
        return rails.getOrPut(rail.id) {
            val job = Job(parentScope.coroutineContext[Job])
            val scope = CoroutineScope(parentScope.coroutineContext + job)
            val pages = load(rail).cachedIn(scope)
            val visiblePages = pages.onStart { retain(rail) }.onCompletion { release(rail) }
                .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
            RailPager(visiblePages, job)
        }.flow
    }

    @Synchronized
    private fun retain(rail: QuizRail) {
        if (rail.snapshotId == snapshotId) rails[rail.id]?.let { it.references++ }
    }

    @Synchronized
    private fun release(rail: QuizRail) {
        if (rail.snapshotId != snapshotId) return
        rails[rail.id]?.let { it.references-- }
        val inactive = rails.entries.filter { it.value.references == 0 }
        inactive.dropLast(MAX_INACTIVE_RAILS).forEach {
            it.value.job.cancel()
            rails.remove(it.key)
        }
    }

    private class RailPager(val flow: Flow<PagingData<QuizCard>>, val job: Job, var references: Int = 0)
}

private const val MAX_INACTIVE_RAILS = 12

private val EmptyLoadStates = LoadStates(
    refresh = LoadState.NotLoading(true),
    prepend = LoadState.NotLoading(true),
    append = LoadState.NotLoading(true),
)
