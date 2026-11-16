package com.quizzgame.core.media.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class IconWeightDto {
    @SerialName("regular")
    REGULAR,

    @SerialName("medium")
    MEDIUM,

    @SerialName("bold")
    BOLD,
}
