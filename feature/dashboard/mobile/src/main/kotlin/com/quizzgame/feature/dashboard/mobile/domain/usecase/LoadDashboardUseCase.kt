package com.quizzgame.feature.dashboard.mobile.domain.usecase

import com.quizzgame.feature.dashboard.mobile.domain.repository.DashboardRepository
import javax.inject.Inject

internal class LoadDashboardUseCase @Inject constructor(private val repository: DashboardRepository) {
    suspend operator fun invoke() = repository.load()
}
