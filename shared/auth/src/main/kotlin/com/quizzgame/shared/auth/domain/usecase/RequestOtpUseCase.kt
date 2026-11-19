package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class RequestOtpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(method: AuthMethod, identifier: String): AuthResult<OtpChallenge> {
        val normalized = normalizeIdentifier(method, identifier)
            ?: return AuthResult.Failed(AuthFailure.InvalidIdentifier)
        return repository.request(method, normalized)
    }
}
