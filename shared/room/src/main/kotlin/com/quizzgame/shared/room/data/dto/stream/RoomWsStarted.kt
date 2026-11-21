package com.quizzgame.shared.room.data.dto.stream

import kotlinx.serialization.Serializable

@Serializable
internal data class RoomWsStarted(
    val gameSessionId: String,
)
