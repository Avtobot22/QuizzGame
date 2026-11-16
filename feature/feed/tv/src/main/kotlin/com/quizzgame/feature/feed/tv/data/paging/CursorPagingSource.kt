package com.quizzgame.feature.feed.tv.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.feature.feed.tv.domain.FeedPagingException
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal abstract class CursorPagingSource<T : Any>(
    private val id: (T) -> String,
    private val onExpired: () -> Boolean,
) : PagingSource<String, T>() {
    private val mutex = Mutex()
    private val ids = mutableSetOf<String>()
    private val cursors = mutableSetOf<String>()
    private val loaded = mutableMapOf<String?, LoadResult.Page<String, T>>()

    final override fun getRefreshKey(state: PagingState<String, T>): String? = null

    protected abstract suspend fun request(cursor: String?): FeedResult<CursorBatch<T>>

    final override suspend fun load(params: LoadParams<String>): LoadResult<String, T> = mutex.withLock {
        if (invalid) return@withLock LoadResult.Invalid()
        if (params is LoadParams.Prepend) return@withLock LoadResult.Page(emptyList(), null, null)
        loaded[params.key]?.let { return@withLock it }
        when (val result = request(params.key)) {
            is FeedResult.Failed -> LoadResult.Error(FeedPagingException(result.failure))
            FeedResult.Expired -> if (onExpired()) {
                invalidate()
                LoadResult.Invalid()
            } else {
                LoadResult.Error(FeedPagingException(AppFailure.InvalidServerData))
            }
            is FeedResult.Loaded -> accept(params.key, result.content)
        }
    }

    private fun accept(key: String?, batch: CursorBatch<T>): LoadResult<String, T> {
        if (invalid) return LoadResult.Invalid()
        val next = batch.page.nextCursor
        val pageIds = batch.items.map(id)
        val validCursor = batch.page.hasMore == (next != null) &&
            (next == null || next.isNotBlank() && next != key && next !in cursors)
        if (!validCursor || pageIds.distinct().size != pageIds.size || pageIds.any { it in ids }) {
            return LoadResult.Error(FeedPagingException(AppFailure.InvalidServerData))
        }
        ids.addAll(pageIds)
        key?.let(cursors::add)
        return LoadResult.Page(batch.items, null, next).also { loaded[key] = it }
    }
}
