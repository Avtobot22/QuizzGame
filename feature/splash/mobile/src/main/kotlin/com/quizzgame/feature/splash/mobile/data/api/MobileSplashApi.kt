package com.quizzgame.feature.splash.mobile.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.splash.data.dto.LaunchDto
import com.quizzgame.shared.splash.data.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Tag

internal interface MobileSplashApi {
    @GET("api/mobile/launch")
    suspend fun launch(@Tag policy: RequestPolicy): LaunchDto

    @GET("api/mobile/user")
    suspend fun currentUser(@Tag policy: RequestPolicy): UserDto
}
