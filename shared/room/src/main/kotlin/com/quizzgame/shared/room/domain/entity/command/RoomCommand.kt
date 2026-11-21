package com.quizzgame.shared.room.domain.entity.command

import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import java.util.UUID

sealed interface RoomCommand {
    data class Ready(val value: Boolean) : RoomCommand
    data class Kick(val participantId: ParticipantId) : RoomCommand
    data class Start(val expectedVersion: Long, val key: UUID) : RoomCommand
    data object Leave : RoomCommand
    data object Close : RoomCommand
}
