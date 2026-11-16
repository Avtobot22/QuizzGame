package com.quizzgame.shared.catalog.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PassingStatusDto {
    @SerialName("notStarted")
    NOT_STARTED,

    @SerialName("inProgress")
    IN_PROGRESS,

    @SerialName("completed")
    COMPLETED,
}
