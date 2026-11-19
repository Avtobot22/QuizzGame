package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class LoadAuthMethodsUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(surface: AuthSurface): AuthResult<List<AuthMethod>> = repository.methods(surface)
}
