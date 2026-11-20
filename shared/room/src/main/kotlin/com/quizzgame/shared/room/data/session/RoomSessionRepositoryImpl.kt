package com.quizzgame.shared.room.data.session

import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomLobby
import com.quizzgame.shared.room.domain.entity.session.RoomSession
import com.quizzgame.shared.room.domain.repository.RoomSessionRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class RoomSessionRepositoryImpl @Inject constructor(
    private val coordinator: RoomSessionCoordinator,
) : RoomSessionRepository {
    override fun observeActiveRoom(): Flow<RoomResult<RoomSession?>> = flow {
        val restored = coordinator.restore()
        if (restored is RoomResult.Failed) emit(restored)
        coordinator.session.collect { emit(RoomResult.Success(it)) }
    }

    override fun observeLobby(id: RoomId): Flow<RoomResult<RoomLobby>> = flow {
        when (val result = coordinator.enterLobby(id)) {
            is RoomResult.Failed -> emit(result)
            is RoomResult.Success -> try {
                result.value.states.collect { stream ->
                    emit(RoomResult.Success(RoomLobby(stream, result.value.self)))
                }
            } finally {
                withContext(NonCancellable) { coordinator.exitLobby(result.value) }
            }
        }
    }

    override suspend fun sendCommand(id: RoomId, command: RoomCommand): RoomResult<Unit> =
        coordinator.sendCommand(id, command)
}
