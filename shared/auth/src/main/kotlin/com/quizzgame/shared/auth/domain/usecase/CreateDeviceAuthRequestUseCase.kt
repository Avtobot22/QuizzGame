package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthRequest
import com.quizzgame.shared.auth.domain.repository.DeviceAuthRepository
import javax.inject.Inject

internal class CreateDeviceAuthRequestUseCase @Inject constructor(private val repository: DeviceAuthRepository) {
    suspend operator fun invoke(
        attempt: SessionSignInAttempt,
        verificationHost: String,
    ): AuthResult<DeviceAuthRequest> = repository.requestDeviceAuth(attempt.deviceId, verificationHost)
}
