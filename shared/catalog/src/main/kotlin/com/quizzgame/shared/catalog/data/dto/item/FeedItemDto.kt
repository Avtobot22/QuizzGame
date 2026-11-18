package com.quizzgame.shared.catalog.data.dto.item

import kotlinx.serialization.Serializable

@Serializable(with = FeedItemSerializer::class)
sealed interface FeedItemDto

@Serializable
data object IgnoredFeedItemDto : FeedItemDto
