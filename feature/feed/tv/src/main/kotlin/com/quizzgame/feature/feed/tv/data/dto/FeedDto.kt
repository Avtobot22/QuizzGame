package com.quizzgame.feature.feed.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class FeedDto(
    val sections: List<FeedSectionDto>,
)
