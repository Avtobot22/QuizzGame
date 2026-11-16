package com.quizzgame.feature.feed.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable(with = FeedSectionSerializer::class)
internal sealed interface FeedSectionDto

@Serializable
internal data object IgnoredFeedSectionDto : FeedSectionDto
