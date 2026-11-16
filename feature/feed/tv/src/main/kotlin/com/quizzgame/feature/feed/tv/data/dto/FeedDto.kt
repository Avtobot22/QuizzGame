package com.quizzgame.feature.feed.tv.data.dto

import com.quizzgame.feature.feed.tv.data.dto.pagination.CursorPageInfoDto
import com.quizzgame.feature.feed.tv.data.dto.pagination.CursorPageInfoSerializer
import com.quizzgame.feature.feed.tv.data.dto.section.FeedSectionDto
import kotlinx.serialization.Serializable

@Serializable
internal data class FeedDto(
    val sections: List<FeedSectionDto>,
    @Serializable(with = CursorPageInfoSerializer::class)
    val page: CursorPageInfoDto,
)
