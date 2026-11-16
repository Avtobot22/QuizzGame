package com.quizzgame.shared.catalog.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BadgeVariantDto {
    @SerialName("default")
    DEFAULT,

    @SerialName("live")
    LIVE,
}
