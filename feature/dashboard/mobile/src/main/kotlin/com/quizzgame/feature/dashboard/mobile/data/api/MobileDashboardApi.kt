package com.quizzgame.feature.dashboard.mobile.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.feature.dashboard.mobile.data.dto.DashboardDto
import retrofit2.http.GET
import retrofit2.http.Tag

internal interface MobileDashboardApi {
    @GET("api/mobile/dashboard")
    suspend fun load(@Tag policy: RequestPolicy): DashboardDto
}
