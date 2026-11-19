package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.provider.YandexAttempt
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import java.time.Instant
import javax.inject.Inject

internal class RedeemYandexAttemptUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(attempt: SessionSignInAttempt, oauth: YandexAttempt): AuthResult<Unit> {
        if (Instant.now() >= oauth.expiresAt) return AuthResult.Failed(AuthFailure.Expired)
        return attempt.commitAuthResult(repository.redeemYandex(oauth, attempt.deviceId))
    }
}
