package com.quizzgame.shared.room.data.dto.admission

import com.quizzgame.shared.room.data.dto.snapshot.SnapshotDto
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateRoomResponse(
    val room: SnapshotDto,
    val credential: CredentialDto,
)
