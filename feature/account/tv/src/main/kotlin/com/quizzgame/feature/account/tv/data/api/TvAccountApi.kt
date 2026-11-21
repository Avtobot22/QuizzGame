package com.quizzgame.feature.account.tv.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.account.data.dto.UpdateNicknameRequest
import com.quizzgame.shared.splash.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Tag

internal interface TvAccountApi {
    @GET("api/tv/user")
    suspend fun profile(@Tag policy: RequestPolicy): UserDto

    @PATCH("api/tv/user")
    suspend fun updateNickname(@Body body: UpdateNicknameRequest, @Tag policy: RequestPolicy): UserDto
}
