package com.quizzgame.feature.dashboard.tv.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.dashboard.tv.data.dto.DashboardDto
import retrofit2.http.GET
import retrofit2.http.Tag

internal interface TvDashboardApi {
    @GET("api/tv/dashboard")
    suspend fun load(@Tag policy: RequestPolicy): DashboardDto
}
