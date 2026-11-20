package com.quizzgame.shared.account.domain.usecase

import com.quizzgame.shared.account.domain.entity.PlayerNickname
import com.quizzgame.shared.account.domain.entity.SavePlayerNicknameResult
import com.quizzgame.shared.account.domain.repository.PlayerProfileRepository
import java.util.UUID
import javax.inject.Inject

/** Проверяет игровой ник и сохраняет его в профиле текущего владельца. */
class SavePlayerNicknameUseCase @Inject internal constructor(private val nicknames: PlayerProfileRepository) {
    suspend operator fun invoke(expectedUserId: UUID, generation: Long, input: String): SavePlayerNicknameResult {
        val name = PlayerNickname.normalize(input) ?: return SavePlayerNicknameResult.InvalidName
        return nicknames.save(expectedUserId, generation, name)
    }
}
