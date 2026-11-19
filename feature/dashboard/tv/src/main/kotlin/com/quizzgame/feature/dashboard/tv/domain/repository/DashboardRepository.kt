package com.quizzgame.feature.dashboard.tv.domain.repository

import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardResult

internal interface DashboardRepository {
    suspend fun load(): DashboardResult
}
