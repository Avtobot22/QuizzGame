package com.quizzgame.shared.room.domain.entity.session

import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
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

/** Последний снимок комнаты, доступный во время подключения. */
val RoomStreamState.visibleSnapshot: RoomSnapshot?
    get() = when (this) {
        is RoomStreamState.Active       -> snapshot
        is RoomStreamState.Reconnecting -> last
        else                            -> null
    }

/** Причина, после которой текущее лобби уже нельзя открыть повторно. */
val RoomStreamState.endReason: RoomFailureReason?
    get() = when (this) {
        is RoomStreamState.Ended  -> reason
        is RoomStreamState.Failed -> failure.reason.takeIf { failure.terminal }
        else                      -> null
    }

/** Комната уже перешла из лобби в игровой сеанс. */
val RoomStreamState.gameStarted: Boolean
    get() = this is RoomStreamState.Started || visibleSnapshot?.phase == RoomPhase.IN_GAME
