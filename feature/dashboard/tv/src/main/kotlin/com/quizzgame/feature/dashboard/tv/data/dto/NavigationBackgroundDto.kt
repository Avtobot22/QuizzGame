package com.quizzgame.feature.dashboard.tv.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class NavigationBackgroundDto {
    @SerialName("solid")
    SOLID,

    @SerialName("translucent")
    TRANSLUCENT,

    @SerialName("transparent")
    TRANSPARENT,
}
