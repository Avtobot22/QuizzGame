package com.quizzgame.shared.feed.data.dto.section

import kotlinx.serialization.Serializable

@Serializable(with = FeedSectionSerializer::class)
sealed interface FeedSectionDto

@Serializable
data object IgnoredFeedSectionDto : FeedSectionDto
