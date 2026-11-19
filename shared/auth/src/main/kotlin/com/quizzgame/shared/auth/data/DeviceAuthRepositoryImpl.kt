package com.quizzgame.shared.auth.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.shared.auth.data.api.DeviceAuthApi
import com.quizzgame.shared.auth.data.dto.DeviceRequestDto
import com.quizzgame.shared.auth.data.dto.device.DeviceAuthRedeemDto
import com.quizzgame.shared.auth.data.mapper.toDomain
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthRequest
import com.quizzgame.shared.auth.domain.repository.DeviceAuthRepository
import javax.inject.Inject

internal class DeviceAuthRepositoryImpl @Inject constructor(private val executor: ApiCallExecutor) :
    DeviceAuthRepository {
    override suspend fun requestDeviceAuth(device: String, verificationHost: String) =
        execute({ requestDeviceAuth(DeviceRequestDto(device), authPolicy) }) { it.toDomain(verificationHost) }

    override suspend fun deviceAuthStatus(request: DeviceAuthRequest) =
        execute({ deviceAuthStatus(request.id, request.secret, authPolicy) }) { it.toDomain() }

    override suspend fun redeemDeviceAuth(request: DeviceAuthRequest, device: String) =
        execute({ redeemDeviceAuth(request.id, DeviceAuthRedeemDto(request.secret, device), authPolicy) }) {
            it.toDomain()
        }

    private suspend fun <D, T> execute(request: suspend DeviceAuthApi.() -> D, map: (D) -> T): AuthResult<T> =
        executor.executeAuthRequest(DeviceAuthApi::class.java, request, map)
}
