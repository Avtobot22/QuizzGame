package com.quizzgame.shared.catalog.domain.entity

data class CursorPageInfo(val hasMore: Boolean, val nextCursor: String?) {
    override fun toString(): String = "CursorPageInfo(hasMore=$hasMore, nextCursor=<redacted>)"
}
