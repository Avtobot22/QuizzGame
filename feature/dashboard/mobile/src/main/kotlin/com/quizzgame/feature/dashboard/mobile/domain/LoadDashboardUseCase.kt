package com.quizzgame.feature.dashboard.mobile.domain

import javax.inject.Inject

internal class LoadDashboardUseCase @Inject constructor(private val repository: DashboardRepository) {
    suspend operator fun invoke() = repository.load()
}
