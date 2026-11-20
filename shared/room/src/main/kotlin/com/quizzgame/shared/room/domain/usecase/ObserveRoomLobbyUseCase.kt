package com.quizzgame.shared.room.domain.usecase

import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.repository.RoomSessionRepository
import javax.inject.Inject

class ObserveRoomLobbyUseCase @Inject internal constructor(private val repository: RoomSessionRepository) {

    operator fun invoke(id: RoomId) = repository.observeLobby(id)
}
