package com.quizzgame.shared.account.domain.usecase

import com.quizzgame.shared.account.domain.repository.PlayerProfileRepository
import javax.inject.Inject

/** Читает актуальный профиль с сервера в действующей сессии. */
class LoadPlayerProfileUseCase @Inject internal constructor(private val profiles: PlayerProfileRepository) {
    suspend operator fun invoke() = profiles.load()
}
