package com.quizzgame.feature.feed.tv.domain.entity

internal data class CursorPageInfo(val hasMore: Boolean, val nextCursor: String?) {

    override fun toString(): String = "CursorPageInfo(hasMore=$hasMore, nextCursor=<redacted>)"
}
