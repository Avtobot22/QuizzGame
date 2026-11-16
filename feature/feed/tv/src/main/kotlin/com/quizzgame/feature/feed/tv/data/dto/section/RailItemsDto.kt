package com.quizzgame.feature.feed.tv.data.dto.section

import com.quizzgame.feature.feed.tv.data.dto.item.FeedItemDto
import com.quizzgame.feature.feed.tv.data.dto.pagination.CursorPageInfoDto
import com.quizzgame.feature.feed.tv.data.dto.pagination.CursorPageInfoSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class RailItemsDto(
    val sectionId: String,
    val items: List<FeedItemDto>,
    @Serializable(with = CursorPageInfoSerializer::class)
    val page: CursorPageInfoDto,
)
