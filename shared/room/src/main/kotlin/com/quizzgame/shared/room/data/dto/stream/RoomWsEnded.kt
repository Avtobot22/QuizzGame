package com.quizzgame.shared.room.data.dto.stream

import kotlinx.serialization.Serializable

@Serializable
internal data class RoomWsEnded(
    val reason: String,
)
