package com.quizzgame.feature.feed.tv.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("rail")
internal data class RailDto(
    val id: String,
    val items: List<FeedItemDto>,
    @Serializable(with = CursorPageInfoSerializer::class)
    val itemsPage: CursorPageInfoDto,
    val title: String? = null,
    val subtitle: String? = null,
) : FeedSectionDto
