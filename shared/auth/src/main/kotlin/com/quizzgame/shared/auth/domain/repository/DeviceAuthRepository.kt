package com.quizzgame.shared.auth.domain.repository

import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthRequest
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthStatus

internal interface DeviceAuthRepository {
    suspend fun requestDeviceAuth(device: String, verificationHost: String): AuthResult<DeviceAuthRequest>
    suspend fun deviceAuthStatus(request: DeviceAuthRequest): AuthResult<DeviceAuthStatus>
    suspend fun redeemDeviceAuth(request: DeviceAuthRequest, device: String): AuthResult<TokenPair>
}
