package com.quizzgame.feature.quizentry.tv.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class AvailabilityStatusDto {
    @SerialName("available")
    AVAILABLE,

    @SerialName("unavailable")
    UNAVAILABLE,
}
