package com.quizzgame.shared.room.data.dto.admission

import com.quizzgame.shared.room.data.dto.snapshot.ParticipantDto
import com.quizzgame.shared.room.data.dto.snapshot.SnapshotDto
import kotlinx.serialization.Serializable

@Serializable
internal data class JoinRoomResponse(
    val room: SnapshotDto,
    val participant: ParticipantDto,
    val credential: CredentialDto,
)
