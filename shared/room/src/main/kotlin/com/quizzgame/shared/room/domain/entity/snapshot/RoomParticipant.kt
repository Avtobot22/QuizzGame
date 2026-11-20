package com.quizzgame.shared.room.domain.entity.snapshot

import com.quizzgame.shared.room.domain.entity.session.ParticipantId

data class RoomParticipant(
    val id: ParticipantId,
    val displayName: String,
    val ready: Boolean,
    val connection: RoomConnection,
)
