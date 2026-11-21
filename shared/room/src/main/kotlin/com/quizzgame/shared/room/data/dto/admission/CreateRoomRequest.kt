package com.quizzgame.shared.room.data.dto.admission

import kotlinx.serialization.Serializable

@Serializable
internal data class CreateRoomRequest(
    val entryId: String,
    val clientInstanceId: String,
)
