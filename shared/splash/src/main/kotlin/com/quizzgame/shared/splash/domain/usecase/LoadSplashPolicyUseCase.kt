package com.quizzgame.shared.splash.domain.usecase

import com.quizzgame.shared.splash.domain.repository.SplashRepository
import javax.inject.Inject

class LoadSplashPolicyUseCase @Inject constructor(private val repository: SplashRepository) {
    suspend operator fun invoke() = repository.loadLaunch()
}
