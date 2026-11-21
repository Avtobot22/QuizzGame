package com.quizzgame.shared.room.domain.entity.session

import java.time.Instant

data class RoomSession(
    val roomId: RoomId,
    val role: RoomRole,
    val expiresAt: Instant,
    val self: ParticipantId?,
    val accountBound: Boolean = false,
)
