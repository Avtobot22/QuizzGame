package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class ResendOtpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(challenge: OtpChallenge): AuthResult<OtpChallenge> = repository.resend(challenge)
}
