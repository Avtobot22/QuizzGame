package com.quizzgame.shared.auth.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.auth.data.dto.DeviceRequestDto
import com.quizzgame.shared.auth.data.dto.TokenPairDto
import com.quizzgame.shared.auth.data.dto.device.DeviceAuthRedeemDto
import com.quizzgame.shared.auth.data.dto.device.DeviceAuthStartDto
import com.quizzgame.shared.auth.data.dto.device.DeviceAuthStatusDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Tag

internal interface DeviceAuthApi {
    @POST("api/auth/device/requests")
    suspend fun requestDeviceAuth(@Body body: DeviceRequestDto, @Tag policy: RequestPolicy): DeviceAuthStartDto

    @GET("api/auth/device/requests/{requestId}")
    suspend fun deviceAuthStatus(
        @Path("requestId") id: String,
        @Header("X-Device-Authorization-Secret") secret: String,
        @Tag policy: RequestPolicy,
    ): DeviceAuthStatusDto

    @POST("api/auth/device/requests/{requestId}/redeem")
    suspend fun redeemDeviceAuth(
        @Path("requestId") id: String,
        @Body body: DeviceAuthRedeemDto,
        @Tag policy: RequestPolicy,
    ): TokenPairDto
}
