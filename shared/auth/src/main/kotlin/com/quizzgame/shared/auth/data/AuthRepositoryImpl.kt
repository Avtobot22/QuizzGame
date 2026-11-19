package com.quizzgame.shared.auth.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.shared.auth.data.api.AuthApi
import com.quizzgame.shared.auth.data.dto.DeviceRequestDto
import com.quizzgame.shared.auth.data.dto.otp.OtpChallengeRequestDto
import com.quizzgame.shared.auth.data.dto.otp.OtpVerifyRequestDto
import com.quizzgame.shared.auth.data.dto.provider.GoogleVerifyRequestDto
import com.quizzgame.shared.auth.data.dto.provider.YandexRedeemRequestDto
import com.quizzgame.shared.auth.data.mapper.toDomain
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.provider.YandexAttempt
import com.quizzgame.shared.auth.domain.repository.AuthRepository
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

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
        executor.executeAuthRequest(AuthApi::class.java, request, map)
}
