package com.quizzgame.shared.room.domain.repository

import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomSession

interface RoomRepository {
    suspend fun create(entryId: String): RoomResult<RoomSession>
    suspend fun join(code: String, nickname: String, accountGeneration: Long?): RoomResult<RoomSession>
}
