package com.quizzgame.core.media.data.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface MediaTransitionDto {
    @Serializable
    @SerialName("none")
    data object None : MediaTransitionDto

    @Serializable
    @SerialName("fade")
    data class Fade(val durationMs: Int = DEFAULT_DURATION_MS) : MediaTransitionDto

    @Serializable
    @SerialName("flipHorizontal")
    data class FlipHorizontal(val durationMs: Int = DEFAULT_DURATION_MS) : MediaTransitionDto

    @Serializable
    @SerialName("flipVertical")
    data class FlipVertical(val durationMs: Int = DEFAULT_DURATION_MS) : MediaTransitionDto
}

private const val DEFAULT_DURATION_MS = 220
