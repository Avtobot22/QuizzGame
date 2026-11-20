package com.quizzgame.shared.room.presentation

import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.snapshot.RoomPhase
import com.quizzgame.shared.room.domain.entity.snapshot.RoomSnapshot

data class RoomLobbyState(
    val stream: RoomStreamState = RoomStreamState.Connecting,
    val self: ParticipantId? = null,
    val sending: Boolean = false,
    val error: RoomFailure? = null,
    val exited: Boolean = false,
    val gameInfo: RoomLobbyGameInfo? = null,
) {
    val snapshot: RoomSnapshot? get() = when (stream) {
        is RoomStreamState.Active -> stream.snapshot
        is RoomStreamState.Reconnecting -> stream.last
        else -> null
    }
    val endReason: RoomFailureReason? get() = when (stream) {
        is RoomStreamState.Ended -> stream.reason
        is RoomStreamState.Failed -> stream.failure.reason.takeIf { stream.failure.terminal }
        else -> null
    }
    val gameStarted: Boolean get() = stream is RoomStreamState.Started || snapshot?.phase == RoomPhase.IN_GAME
    val live: Boolean get() = stream is RoomStreamState.Active
}
