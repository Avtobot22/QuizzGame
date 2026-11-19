package com.quizzgame.feature.dashboard.tv.domain.usecase

import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardResult
import com.quizzgame.feature.dashboard.tv.domain.repository.DashboardRepository
import javax.inject.Inject

internal class LoadDashboardUseCase @Inject constructor(
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(): DashboardResult = repository.load()
}
