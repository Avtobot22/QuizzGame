package com.quizzgame.feature.feed.tv.data.dto.item

import kotlinx.serialization.Serializable

@Serializable(with = FeedItemSerializer::class)
internal sealed interface FeedItemDto

@Serializable
internal data object IgnoredFeedItemDto : FeedItemDto
