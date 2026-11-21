package com.quizzgame.shared.room.presentation

import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.session.RoomId

sealed interface RoomLobbyIntent {
    data class Send(val id: RoomId, val command: RoomCommand) : RoomLobbyIntent
    data object Retry : RoomLobbyIntent
}
