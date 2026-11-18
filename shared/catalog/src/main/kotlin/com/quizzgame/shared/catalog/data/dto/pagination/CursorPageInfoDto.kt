package com.quizzgame.shared.catalog.data.dto.pagination

import kotlinx.serialization.Serializable

@Serializable
data class CursorPageInfoDto(val hasMore: Boolean, val nextCursor: String?) {
    override fun toString(): String = "CursorPageInfoDto(hasMore=$hasMore, nextCursor=<redacted>)"
}
