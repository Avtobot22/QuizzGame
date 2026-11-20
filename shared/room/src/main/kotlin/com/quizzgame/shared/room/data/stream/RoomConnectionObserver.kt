package com.quizzgame.shared.room.data.stream

import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot

internal interface RoomConnectionObserver {
    suspend fun lastSnapshot(): RoomSnapshot?
    suspend fun publish(value: RoomSnapshot, connected: Boolean)
    suspend fun update(state: RoomStreamState)
    suspend fun end(reason: RoomFailureReason)
}
