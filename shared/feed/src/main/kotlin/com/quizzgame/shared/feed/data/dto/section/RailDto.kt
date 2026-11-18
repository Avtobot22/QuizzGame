package com.quizzgame.shared.feed.data.dto.section

import com.quizzgame.shared.catalog.data.dto.item.FeedItemDto
import com.quizzgame.shared.catalog.data.dto.pagination.CursorPageInfoDto
import com.quizzgame.shared.catalog.data.dto.pagination.CursorPageInfoSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("rail")
data class RailDto(
    val id: String,
    val items: List<FeedItemDto>,
    @Serializable(with = CursorPageInfoSerializer::class)
    val itemsPage: CursorPageInfoDto,
    val title: String? = null,
    val subtitle: String? = null,
) : FeedSectionDto
