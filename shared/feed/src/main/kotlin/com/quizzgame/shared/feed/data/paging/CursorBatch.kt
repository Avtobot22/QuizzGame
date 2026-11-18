package com.quizzgame.shared.feed.data.paging

import com.quizzgame.shared.catalog.domain.entity.CursorPageInfo
import com.quizzgame.shared.feed.domain.entity.FeedResult

data class CursorBatch<T>(val items: List<T>, val page: CursorPageInfo)

inline fun <T, R> FeedResult<T>.mapContent(transform: (T) -> R): FeedResult<R> = when (this) {
    is FeedResult.Loaded -> FeedResult.Loaded(transform(content))
    is FeedResult.Failed -> FeedResult.Failed(failure)
    FeedResult.Expired -> FeedResult.Expired
}
