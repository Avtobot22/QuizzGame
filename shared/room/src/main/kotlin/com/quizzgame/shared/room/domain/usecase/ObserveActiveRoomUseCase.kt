package com.quizzgame.shared.room.domain.usecase

import com.quizzgame.shared.room.domain.repository.RoomSessionRepository
import javax.inject.Inject

/** Восстанавливает комнату устройства и сообщает об изменениях её сессии. */
class ObserveActiveRoomUseCase @Inject internal constructor(private val repository: RoomSessionRepository) {
    operator fun invoke() = repository.observeActiveRoom()
}
