package com.quizzgame.shared.auth.data

import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.shared.auth.data.api.AuthApi
import com.quizzgame.shared.auth.data.dto.DeviceRequestDto
import com.quizzgame.shared.auth.data.dto.GoogleVerifyRequestDto
import com.quizzgame.shared.auth.data.dto.OtpChallengeRequestDto
import com.quizzgame.shared.auth.data.dto.OtpVerifyRequestDto
import com.quizzgame.shared.auth.data.dto.YandexRedeemRequestDto
import com.quizzgame.shared.auth.data.mapper.toAuthFailure
import com.quizzgame.shared.auth.data.mapper.toDomain
import com.quizzgame.shared.auth.domain.AuthRepository
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.YandexAttempt
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

private val authPolicy = RequestPolicy(AuthPolicy.NONE, ReplayPolicy.NEVER)

internal class AuthRepositoryImpl @Inject constructor(private val executor: ApiCallExecutor) : AuthRepository {
    override suspend fun methods(surface: AuthSurface) = execute({ methods(surface.wireId, authPolicy) }) {
        it.toDomain()
    }

    override suspend fun request(method: AuthMethod, identifier: String) = execute(
        { request(OtpChallengeRequestDto(method.wireId, identifier), authPolicy) },
    ) { it.toDomain() }

    override suspend fun resend(challenge: OtpChallenge) = execute({ resend(challenge.id, authPolicy) }) {
        it.toDomain().also { new -> require(new.id != challenge.id) }
    }

    override suspend fun verify(id: String, code: String, device: String, key: String) = execute(
        { verify(id, OtpVerifyRequestDto(code, device), key, authPolicy) },
    ) { it.toDomain() }

    override suspend fun googleChallenge() = execute({ googleChallenge(JsonObject(emptyMap()), authPolicy) }) {
        it.toDomain()
    }

    override suspend fun verifyGoogle(id: String, token: String, device: String) = execute(
        { verifyGoogle(GoogleVerifyRequestDto(id, token, device), authPolicy) },
    ) { it.toDomain() }

    override suspend fun yandexAttempt(device: String) =
        execute({ yandexAttempt(DeviceRequestDto(device), authPolicy) }) { it.toDomain() }

    override suspend fun redeemYandex(attempt: YandexAttempt, device: String) = execute(
        { redeemYandex(attempt.id, YandexRedeemRequestDto(attempt.secret, device), authPolicy) },
    ) { it.toDomain() }

    private suspend fun <D, T> execute(request: suspend AuthApi.() -> D, map: (D) -> T): AuthResult<T> =
        when (val call = executor.call(AuthApi::class.java, request)) {
            is ApiCallResult.Executed -> when (val result = call.result) {
                is NetworkResult.Failure -> AuthResult.Failed(result.reason.toAuthFailure())
                is NetworkResult.Success -> try {
                    AuthResult.Success(map(result.value))
                } catch (_: IllegalArgumentException) {
                    AuthResult.Failed(AuthFailure.General(AppFailure.InvalidServerData))
                }
            }
            is ApiCallResult.Unavailable -> AuthResult.Failed(AuthFailure.General(classifyApiUnavailable(call.reason)))
            ApiCallResult.Closed -> AuthResult.Failed(AuthFailure.General(AppFailure.OperationInterrupted))
        }
}
