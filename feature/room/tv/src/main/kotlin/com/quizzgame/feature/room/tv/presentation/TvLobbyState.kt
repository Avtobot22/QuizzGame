package com.quizzgame.feature.room.tv.presentation

import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.entity.session.endReason
import com.quizzgame.shared.room.domain.entity.session.gameStarted
import com.quizzgame.shared.room.domain.entity.session.visibleSnapshot
import com.quizzgame.shared.room.presentation.RoomLobbyCommandError

internal data class TvLobbyState(
    val stream: RoomStreamState = RoomStreamState.Connecting,
    val sending: Boolean = false,
    val commandError: RoomLobbyCommandError<TvLobbyIntent.Command>? = null,
    val exited: Boolean = false,
) {
    val snapshot get() = stream.visibleSnapshot
    val endReason get() = stream.endReason
    val gameStarted get() = stream.gameStarted
    val live get() = stream is RoomStreamState.Active
}
