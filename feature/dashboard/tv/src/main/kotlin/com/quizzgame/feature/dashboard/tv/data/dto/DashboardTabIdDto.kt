package com.quizzgame.feature.dashboard.tv.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class DashboardTabIdDto {
    @SerialName("feed")
    FEED,

    @SerialName("library")
    LIBRARY,

    @SerialName("settings")
    SETTINGS,
}
