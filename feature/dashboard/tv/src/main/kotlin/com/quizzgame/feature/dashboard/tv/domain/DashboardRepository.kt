package com.quizzgame.feature.dashboard.tv.domain

import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardResult

internal interface DashboardRepository {
    suspend fun load(): DashboardResult
}
