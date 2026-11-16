package com.quizzgame.feature.feed.tv.data.dto

import com.quizzgame.core.media.data.dto.MediaDto
import com.quizzgame.shared.catalog.data.dto.BadgeDto
import com.quizzgame.shared.catalog.data.dto.UserQuizStateDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("quiz")
internal data class QuizCardDto(
    val id: String,
    val entryId: String,
    val title: String,
    val image: MediaDto,
    val user: UserQuizStateDto,
    val subtitle: String? = null,
    val style: QuizCardStyleDto? = null,
    val badge: BadgeDto? = null,
) : FeedItemDto
