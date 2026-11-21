package com.quizzgame.shared.room.domain.entity.snapshot

import com.quizzgame.shared.room.domain.entity.session.RoomId
import java.time.Instant

data class RoomSnapshot(
    val roomId: RoomId,
    val entry: RoomEntry,
    val phase: RoomPhase,
    val version: Long,
    val capacity: RoomCapacity,
    val policy: RoomPolicy,
    val players: List<RoomParticipant>,
    val permissions: RoomPermissions,
    val expiresAt: Instant,
    val join: RoomInvite? = null,
)
