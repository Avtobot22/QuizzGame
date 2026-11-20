package com.quizzgame.shared.room.domain.usecase

import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.repository.RoomSessionRepository
import javax.inject.Inject

/** Отправляет команду от имени текущего участника или ведущего. */
class SendRoomCommandUseCase @Inject internal constructor(private val repository: RoomSessionRepository) {
    suspend operator fun invoke(id: RoomId, command: RoomCommand) = repository.sendCommand(id, command)
}
