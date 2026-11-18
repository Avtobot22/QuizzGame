package com.quizzgame.shared.feed.data.dto

import com.quizzgame.shared.catalog.data.dto.pagination.CursorPageInfoDto
import com.quizzgame.shared.catalog.data.dto.pagination.CursorPageInfoSerializer
import com.quizzgame.shared.feed.data.dto.section.FeedSectionDto
import kotlinx.serialization.Serializable

@Serializable
data class FeedDto(
    val sections: List<FeedSectionDto>,
    @Serializable(with = CursorPageInfoSerializer::class)
    val page: CursorPageInfoDto,
)
