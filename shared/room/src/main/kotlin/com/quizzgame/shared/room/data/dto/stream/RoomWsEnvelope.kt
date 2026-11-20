package com.quizzgame.shared.room.data.dto.stream

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class RoomWsEnvelope(
    val type: String,
    val version: Long,
    val sentAt: String,
    val payload: JsonElement,
)
