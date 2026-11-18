package com.quizzgame.shared.catalog.data.dto.entry

import com.quizzgame.core.media.data.dto.ColorDto
import com.quizzgame.core.media.data.dto.MediaDto
import com.quizzgame.shared.catalog.data.dto.BadgeDto
import com.quizzgame.shared.catalog.data.dto.UserQuizStateDto
import kotlinx.serialization.Serializable

@Serializable
data class QuizEntryDto(
    val entryId: String,
    val title: String,
    val cover: MediaDto,
    val user: UserQuizStateDto,
    val availability: AvailabilityDto,
    val subtitle: String? = null,
    val description: String? = null,
    val accentColor: ColorDto? = null,
    val badge: BadgeDto? = null,
    val stats: QuizStatsDto? = null,
    val primaryAction: PrimaryActionDto? = null,
)
