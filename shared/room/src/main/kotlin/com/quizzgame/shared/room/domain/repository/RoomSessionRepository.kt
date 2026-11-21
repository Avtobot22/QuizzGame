package com.quizzgame.shared.room.domain.repository

import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomLobby
import com.quizzgame.shared.room.domain.entity.session.RoomSession
import kotlinx.coroutines.flow.Flow

internal interface RoomSessionRepository {

    fun observeActiveRoom(): Flow<RoomResult<RoomSession?>>

    fun observeLobby(id: RoomId): Flow<RoomResult<RoomLobby>>

    suspend fun sendCommand(id: RoomId, command: RoomCommand): RoomResult<Unit>
}
