package com.quizzgame.shared.feed.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.feed.domain.entity.FeedPagingException
import com.quizzgame.shared.feed.domain.entity.FeedResult
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Хранит только связи курсоров и ID; вытесненные Paging-страницы можно прочитать назад. */
abstract class CursorPagingSource<T : Any>(
    private val id: (T) -> String,
    private val onExpired: () -> Boolean,
) : PagingSource<CursorKey, T>() {
    private val ids = mutableSetOf<String>()
    private val pages = LinkedHashMap<CursorKey, PageRecord>()

    final override fun getRefreshKey(state: PagingState<CursorKey, T>): CursorKey? = null
    protected abstract suspend fun request(cursor: String?): FeedResult<CursorBatch<T>>

    final override suspend fun load(params: LoadParams<CursorKey>): LoadResult<CursorKey, T> {
        if (invalid) return LoadResult.Invalid()
        val key = params.key ?: CursorKey.Start
        val cursor = (key as? CursorKey.After)?.cursor
        return try {
            when (val result = request(cursor)) {
                is FeedResult.Failed -> LoadResult.Error(FeedPagingException(result.failure))
                FeedResult.Expired -> expired()
                is FeedResult.Loaded -> accept(key, result.content)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            LoadResult.Error(failure)
        }
    }

    private fun expired(): LoadResult<CursorKey, T> = if (onExpired()) {
        invalidate()
        LoadResult.Invalid()
    } else {
        invalidData()
    }

    private fun accept(key: CursorKey, batch: CursorBatch<T>): LoadResult<CursorKey, T> {
        if (invalid) return LoadResult.Invalid()
        val next = batch.page.nextCursor?.let { CursorKey.After(it) }
        val pageIds = batch.items.map(id)
        val prior = pages[key]
        val validCursor = validCursor(key, next, batch.page.hasMore, prior?.next)
        val unique = pageIds.distinct().size == pageIds.size &&
            if (prior == null) pageIds.none { it in ids } else prior.ids == pageIds && prior.next == next
        if (!validCursor || !unique) return invalidData()
        val previous = prior?.previous ?: if (key == CursorKey.Start) null else pages.keys.lastOrNull()
        pages[key] = PageRecord(previous, next, pageIds)
        ids.addAll(pageIds)
        return LoadResult.Page(batch.items, previous, next)
    }

    private fun validCursor(key: CursorKey, next: CursorKey.After?, hasMore: Boolean, knownNext: CursorKey?): Boolean =
        hasMore == (next != null) &&
            (next == null || next.cursor.isNotBlank() && next != key && (next !in pages || knownNext == next))

    private fun invalidData() = LoadResult.Error<CursorKey, T>(FeedPagingException(AppFailure.InvalidServerData))
    private data class PageRecord(val previous: CursorKey?, val next: CursorKey?, val ids: List<String>)
}
