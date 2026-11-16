package com.quizzgame.feature.feed.tv.data.paging

import com.quizzgame.feature.feed.tv.domain.entity.CursorPageInfo
import com.quizzgame.feature.feed.tv.domain.entity.FeedResult

internal data class CursorBatch<T>(val items: List<T>, val page: CursorPageInfo)

internal inline fun <T, R> FeedResult<T>.mapContent(transform: (T) -> R): FeedResult<R> = when (this) {
    is FeedResult.Loaded -> FeedResult.Loaded(transform(content))
    is FeedResult.Failed -> FeedResult.Failed(failure)
    FeedResult.Expired -> FeedResult.Expired
}
