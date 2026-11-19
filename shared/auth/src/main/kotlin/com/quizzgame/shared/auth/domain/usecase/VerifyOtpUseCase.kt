package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class VerifyOtpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(
        attempt: SessionSignInAttempt,
        id: String,
        code: String,
        key: String,
    ): AuthResult<Unit> = attempt.commitAuthResult(repository.verify(id, code, attempt.deviceId, key))
}
