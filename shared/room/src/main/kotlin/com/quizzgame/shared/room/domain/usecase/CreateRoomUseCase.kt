package com.quizzgame.shared.room.domain.usecase

import com.quizzgame.shared.room.domain.repository.RoomRepository
import javax.inject.Inject

class CreateRoomUseCase @Inject constructor(private val repository: RoomRepository) {
    suspend operator fun invoke(entryId: String) = repository.create(entryId)
}
