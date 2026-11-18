package com.quizzgame.shared.auth.data.api

import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.shared.auth.data.dto.AuthMethodsDto
import com.quizzgame.shared.auth.data.dto.DeviceRequestDto
import com.quizzgame.shared.auth.data.dto.GoogleChallengeDto
import com.quizzgame.shared.auth.data.dto.GoogleVerifyRequestDto
import com.quizzgame.shared.auth.data.dto.OtpChallengeDto
import com.quizzgame.shared.auth.data.dto.OtpChallengeRequestDto
import com.quizzgame.shared.auth.data.dto.OtpVerifyRequestDto
import com.quizzgame.shared.auth.data.dto.TokenPairDto
import com.quizzgame.shared.auth.data.dto.YandexAttemptDto
import com.quizzgame.shared.auth.data.dto.YandexRedeemRequestDto
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

internal interface AuthApi {
    @GET("api/v2/auth/methods")
    suspend fun methods(@Query("clientSurface") surface: String, @Tag policy: RequestPolicy): AuthMethodsDto

    @POST("api/v2/auth/otp/challenges")
    suspend fun request(@Body body: OtpChallengeRequestDto, @Tag policy: RequestPolicy): OtpChallengeDto

    @POST("api/v2/auth/otp/challenges/{challengeId}/verify")
    suspend fun verify(
        @Path("challengeId") id: String,
        @Body body: OtpVerifyRequestDto,
        @Header("Idempotency-Key") key: String,
        @Tag policy: RequestPolicy,
    ): TokenPairDto

    @POST("api/v2/auth/otp/challenges/{challengeId}/resend")
    suspend fun resend(@Path("challengeId") id: String, @Tag policy: RequestPolicy): OtpChallengeDto

    @POST("api/v2/auth/oauth/google/challenges")
    suspend fun googleChallenge(@Body body: JsonObject, @Tag policy: RequestPolicy): GoogleChallengeDto

    @POST("api/v2/auth/oauth/google/verify")
    suspend fun verifyGoogle(@Body body: GoogleVerifyRequestDto, @Tag policy: RequestPolicy): TokenPairDto

    @POST("api/v2/auth/oauth/yandex/attempts")
    suspend fun yandexAttempt(@Body body: DeviceRequestDto, @Tag policy: RequestPolicy): YandexAttemptDto

    @POST("api/v2/auth/oauth/yandex/attempts/{attemptId}/redeem")
    suspend fun redeemYandex(
        @Path("attemptId") id: String,
        @Body body: YandexRedeemRequestDto,
        @Tag policy: RequestPolicy,
    ): TokenPairDto
}
