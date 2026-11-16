package com.quizzgame.core.media.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaScaleDto {
    @SerialName("cover")
    COVER,

    @SerialName("contain")
    CONTAIN,
}
