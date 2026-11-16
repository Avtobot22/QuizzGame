package com.quizzgame.shared.catalog.data.dto

import com.quizzgame.core.media.data.dto.ColorDto
import kotlinx.serialization.Serializable

@Serializable
data class BadgeDto(
    val text: String,
    val variant: BadgeVariantDto,
    val textColor: ColorDto? = null,
    val backgroundColor: ColorDto? = null,
)
