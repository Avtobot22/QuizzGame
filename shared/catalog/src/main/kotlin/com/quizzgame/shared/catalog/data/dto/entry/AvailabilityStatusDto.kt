package com.quizzgame.shared.catalog.data.dto.entry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AvailabilityStatusDto {
    @SerialName("available")
    AVAILABLE,

    @SerialName("unavailable")
    UNAVAILABLE,
}
