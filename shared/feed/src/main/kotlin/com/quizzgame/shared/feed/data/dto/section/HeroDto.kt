package com.quizzgame.shared.feed.data.dto.section

import com.quizzgame.shared.catalog.data.dto.item.FeedItemDto
import kotlinx.serialization.Serializable

@Serializable
data class HeroDto(val id: String, val item: FeedItemDto) : FeedSectionDto
