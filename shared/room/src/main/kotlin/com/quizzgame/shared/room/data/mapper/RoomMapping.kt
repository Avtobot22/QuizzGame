package com.quizzgame.shared.room.data.mapper

import com.quizzgame.shared.room.data.dto.admission.CredentialDto
import com.quizzgame.shared.room.data.dto.snapshot.InviteDto
import com.quizzgame.shared.room.data.dto.snapshot.ParticipantDto
import com.quizzgame.shared.room.data.dto.snapshot.SnapshotDto
import com.quizzgame.shared.room.data.storage.RoomCredential
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinInput
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import com.quizzgame.shared.room.domain.entity.session.roomUuid
import com.quizzgame.shared.room.domain.entity.snapshot.RoomCapacity
import com.quizzgame.shared.room.domain.entity.snapshot.RoomConnection
import com.quizzgame.shared.room.domain.entity.snapshot.RoomEntry
import com.quizzgame.shared.room.domain.entity.snapshot.RoomInvite
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPermissions
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPolicy
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import java.time.Instant
import java.time.format.DateTimeParseException

internal fun SnapshotDto.toDomain(role: RoomRole, links: RoomJoinLink): RoomSnapshot {
    val room = requireNotNull(RoomId.parse(roomId))
    val phase = when (state) {
        "lobby" -> RoomPhase.LOBBY
        "starting" -> RoomPhase.STARTING
        "inGame" -> RoomPhase.IN_GAME
        "closed" -> RoomPhase.CLOSED
        else -> invalidRoomData()
    }
    val roster = players.map(ParticipantDto::toDomain)
    require(version >= 0 && capacity.minPlayers in 1..capacity.maxPlayers)
    require(roster.size <= capacity.maxPlayers && roster.distinctBy { it.id }.size == roster.size)
    require(entry.title.isNotBlank())
    val actions = permissions.run { RoomPermissions(canStart, canClose, canKick, canReady) }
    require(actions.validFor(role))
    require(role == RoomRole.HOST || join == null)
    return RoomSnapshot(
        roomId = room,
        entry = RoomEntry(requireNotNull(entry.entryId.roomUuid()), entry.title),
        phase = phase,
        version = version,
        capacity = RoomCapacity(capacity.minPlayers, capacity.maxPlayers),
        policy = RoomPolicy(policy.guestPlayersAllowed, policy.allPlayersReadyRequired),
        players = roster,
        permissions = actions,
        expiresAt = roomInstant(expiresAt),
        join = join?.toDomain(links),
    )
}

private fun RoomPermissions.validFor(role: RoomRole): Boolean = when (role) {
    RoomRole.HOST -> !canReady
    RoomRole.PARTICIPANT -> !canStart && !canClose && !canKick
}

private fun InviteDto.toDomain(links: RoomJoinLink): RoomInvite {
    require(RoomJoinInput.validCode(code))
    return RoomInvite(code, url.takeIf { links.codeFrom(it) == code }, roomInstant(expiresAt))
}

internal fun ParticipantDto.toDomain(): RoomParticipant = RoomParticipant(
    id = ParticipantId(requireNotNull(participantId.roomUuid())),
    displayName = requireNotNull(RoomJoinInput.nickname(displayName)),
    ready = ready,
    connection = when (connection) {
        "connected" -> RoomConnection.CONNECTED
        "reconnecting" -> RoomConnection.RECONNECTING
        "disconnected" -> RoomConnection.DISCONNECTED
        else -> invalidRoomData()
    },
)

internal fun CredentialDto.toDomain(): RoomCredential {
    val roomRole = when (role) {
        "host" -> RoomRole.HOST
        "participant" -> RoomRole.PARTICIPANT
        else -> invalidRoomData()
    }
    require(roomToken.isNotEmpty() && roomToken.all { it in '!'..'~' } && !RoomJoinInput.validCode(roomToken))
    return RoomCredential(roomRole, roomToken, roomInstant(expiresAt))
}

internal fun roomInstant(value: String): Instant = try {
    Instant.parse(value)
} catch (_: DateTimeParseException) {
    invalidRoomData()
}

internal fun invalidRoomData(): Nothing = throw IllegalArgumentException("Invalid room protocol data")
