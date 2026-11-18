package com.quizzgame.shared.feed.data.dto.section

import com.quizzgame.shared.catalog.data.dto.item.FeedItemDto
import com.quizzgame.shared.catalog.data.dto.pagination.CursorPageInfoDto
import com.quizzgame.shared.catalog.data.dto.pagination.CursorPageInfoSerializer
import kotlinx.serialization.Serializable

@Serializable
data class RailItemsDto(
    val sectionId: String,
    val items: List<FeedItemDto>,
    @Serializable(with = CursorPageInfoSerializer::class)
    val page: CursorPageInfoDto,
)
