package com.quizzgame.feature.dashboard.tv.domain

import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardResult
import javax.inject.Inject

internal class LoadDashboardUseCase @Inject constructor(
    private val repository: DashboardRepository,
) : suspend () -> DashboardResult by repository::load
