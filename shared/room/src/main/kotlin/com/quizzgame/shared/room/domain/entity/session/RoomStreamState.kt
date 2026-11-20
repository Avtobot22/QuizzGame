package com.quizzgame.shared.room.domain.entity.session

import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot
import java.util.UUID

sealed interface RoomStreamState {
    data object Connecting : RoomStreamState
    data class Active(val snapshot: RoomSnapshot) : RoomStreamState
    data class Reconnecting(val last: RoomSnapshot?) : RoomStreamState
    data class Ended(val roomId: RoomId, val reason: RoomFailureReason) : RoomStreamState
    data class Failed(val failure: RoomFailure) : RoomStreamState
    data class Started(val gameSessionId: UUID) : RoomStreamState
}
