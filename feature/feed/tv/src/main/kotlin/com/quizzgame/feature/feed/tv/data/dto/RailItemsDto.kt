package com.quizzgame.feature.feed.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class RailItemsDto(
    val sectionId: String,
    val items: List<FeedItemDto>,
    @Serializable(with = CursorPageInfoSerializer::class)
    val page: CursorPageInfoDto,
)
