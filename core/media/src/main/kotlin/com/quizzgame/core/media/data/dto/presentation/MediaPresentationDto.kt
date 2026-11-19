package com.quizzgame.core.media.data.dto.presentation

import com.quizzgame.core.media.data.dto.ColorDto
import kotlinx.serialization.Serializable

@Serializable
data class MediaPresentationDto(
    val scale: MediaScaleDto = MediaScaleDto.COVER,
    val aspectRatio: Float? = null,
    val placeholder: String? = null,
    val transition: MediaTransitionDto? = null,
    val blurRadius: Float = 0f,
    val tint: ColorDto? = null,
)
