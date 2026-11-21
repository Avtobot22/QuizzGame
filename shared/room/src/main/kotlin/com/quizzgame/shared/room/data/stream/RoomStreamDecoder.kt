package com.quizzgame.shared.room.data.stream

import com.quizzgame.shared.room.data.dto.snapshot.SnapshotDto
import com.quizzgame.shared.room.data.dto.stream.RoomWsEnded
import com.quizzgame.shared.room.data.dto.stream.RoomWsEnvelope
import com.quizzgame.shared.room.data.dto.stream.RoomWsStarted
import com.quizzgame.shared.room.data.mapper.roomInstant
import com.quizzgame.shared.room.data.mapper.toDomain
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import com.quizzgame.shared.room.domain.entity.session.roomUuid
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject

internal class RoomStreamDecoder @Inject constructor(private val json: Json, private val links: RoomJoinLink) {
    fun decode(text: String, role: RoomRole): RoomStreamEvent? = try {
        require(text.length <= MAX_FRAME_LENGTH)
        val event = json.decodeFromString<RoomWsEnvelope>(text)
        require(event.version >= 0)
        roomInstant(event.sentAt)
        when (event.type) {
            "room.snapshot" -> {
                val snapshot = json.decodeFromJsonElement<SnapshotDto>(event.payload).toDomain(role, links)
                require(snapshot.version == event.version)
                RoomStreamEvent.Snapshot(snapshot)
            }
            "room.closed" -> ended(event, RoomFailureReason.CLOSED)
            "room.removed" -> ended(event, RoomFailureReason.REMOVED)
            "room.started" -> {
                val gameId = json.decodeFromJsonElement<RoomWsStarted>(event.payload).gameSessionId
                RoomStreamEvent.Started(event.version, requireNotNull(gameId.roomUuid()))
            }
            else -> null
        }
    } catch (_: IllegalArgumentException) {
        RoomStreamEvent.Invalid
    }

    private fun ended(event: RoomWsEnvelope, reason: RoomFailureReason): RoomStreamEvent.Ended {
        require(json.decodeFromJsonElement<RoomWsEnded>(event.payload).reason.isNotBlank())
        return RoomStreamEvent.Ended(event.version, reason)
    }

    private companion object {
        const val MAX_FRAME_LENGTH = 256 * 1024
    }
}
