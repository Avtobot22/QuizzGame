package com.quizzgame.shared.room.domain.usecase

import com.quizzgame.shared.room.domain.repository.RoomRepository
import javax.inject.Inject

class JoinRoomUseCase @Inject constructor(private val repository: RoomRepository) {
    suspend operator fun invoke(code: String, nickname: String, accountGeneration: Long?) =
        repository.join(code, nickname, accountGeneration)
}
