package com.quizzgame.feature.dashboard.mobile.domain.repository

import com.quizzgame.feature.dashboard.mobile.domain.entity.DashboardResult

internal interface DashboardRepository {
    suspend fun load(): DashboardResult
}
