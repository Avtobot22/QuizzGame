package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.provider.YandexAttempt
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class CreateYandexAttemptUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(attempt: SessionSignInAttempt): AuthResult<YandexAttempt> =
        repository.yandexAttempt(attempt.deviceId)
}
