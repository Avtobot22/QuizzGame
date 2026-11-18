package com.quizzgame.feature.dashboard.mobile.domain

import com.quizzgame.feature.dashboard.mobile.domain.entity.DashboardResult

internal interface DashboardRepository {
    suspend fun load(): DashboardResult
}
