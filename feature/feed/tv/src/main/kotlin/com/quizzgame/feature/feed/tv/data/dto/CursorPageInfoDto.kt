package com.quizzgame.feature.feed.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class CursorPageInfoDto(val hasMore: Boolean, val nextCursor: String?) {
    override fun toString(): String = "CursorPageInfoDto(hasMore=$hasMore, nextCursor=<redacted>)"
}
