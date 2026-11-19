package com.quizzgame.feature.dashboard.mobile.domain.usecase

import com.quizzgame.feature.dashboard.mobile.domain.repository.AccountRepository
import javax.inject.Inject

internal class LoadAccountUseCase @Inject constructor(private val repository: AccountRepository) {
    suspend operator fun invoke() = repository.current()
}
