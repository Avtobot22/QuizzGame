package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.provider.GoogleChallenge
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class CreateGoogleChallengeUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): AuthResult<GoogleChallenge> = repository.googleChallenge()
}
