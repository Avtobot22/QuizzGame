package com.quizzgame.shared.room.data.dto.snapshot

import kotlinx.serialization.Serializable

@Serializable
internal data class SnapshotDto(
    val roomId: String,
    val entry: EntryDto,
    val state: String,
    val version: Long,
    val capacity: CapacityDto,
    val policy: PolicyDto,
    val players: List<ParticipantDto>,
    val permissions: PermissionsDto,
    val expiresAt: String,
    val join: InviteDto? = null,
)
