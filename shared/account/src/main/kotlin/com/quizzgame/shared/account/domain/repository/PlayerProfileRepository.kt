package com.quizzgame.shared.account.domain.repository

import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.domain.entity.SavePlayerNicknameResult
import java.util.UUID

internal interface PlayerProfileRepository {
    suspend fun load(): PlayerProfile
    suspend fun save(userId: UUID, generation: Long, nickname: String): SavePlayerNicknameResult
}
