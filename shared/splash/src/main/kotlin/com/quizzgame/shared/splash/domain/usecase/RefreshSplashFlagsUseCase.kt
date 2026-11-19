package com.quizzgame.shared.splash.domain.usecase

import com.quizzgame.shared.splash.domain.repository.SplashRepository
import javax.inject.Inject

class RefreshSplashFlagsUseCase @Inject constructor(private val repository: SplashRepository) {
    suspend operator fun invoke() = repository.refreshFlags()
}
