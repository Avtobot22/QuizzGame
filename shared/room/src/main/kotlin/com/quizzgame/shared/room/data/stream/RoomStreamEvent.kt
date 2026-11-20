package com.quizzgame.shared.room.data.stream

import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import java.util.UUID

internal sealed interface RoomStreamEvent {
    data class Snapshot(val value: RoomSnapshot) : RoomStreamEvent
    data class Ended(val version: Long, val reason: RoomFailureReason) : RoomStreamEvent
    data class Started(val version: Long, val gameSessionId: UUID) : RoomStreamEvent
    data object Invalid : RoomStreamEvent
}
