package com.quizzgame.feature.account.mobile.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.account.data.dto.UpdateNicknameRequest
import com.quizzgame.shared.splash.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Tag

internal interface MobileAccountApi {
    @GET("api/mobile/user")
    suspend fun profile(@Tag policy: RequestPolicy): UserDto

    @PATCH("api/mobile/user")
    suspend fun updateNickname(@Body body: UpdateNicknameRequest, @Tag policy: RequestPolicy): UserDto
}
