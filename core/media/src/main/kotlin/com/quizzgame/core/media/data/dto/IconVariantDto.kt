package com.quizzgame.core.media.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class IconVariantDto {
    @SerialName("outline")
    OUTLINE,

    @SerialName("filled")
    FILLED,

    @SerialName("twoTone")
    TWO_TONE,
}
