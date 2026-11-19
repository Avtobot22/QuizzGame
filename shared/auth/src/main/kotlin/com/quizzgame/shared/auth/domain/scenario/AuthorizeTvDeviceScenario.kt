package com.quizzgame.shared.auth.domain.scenario

import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthRequest
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthorization
import com.quizzgame.shared.auth.domain.entity.device.DeviceVerificationHost
import com.quizzgame.shared.auth.domain.usecase.AwaitDeviceApprovalUseCase
import com.quizzgame.shared.auth.domain.usecase.BeginSignInUseCase
import com.quizzgame.shared.auth.domain.usecase.CreateDeviceAuthRequestUseCase
import com.quizzgame.shared.auth.domain.usecase.RedeemDeviceAuthRequestUseCase
import java.time.Instant
import javax.inject.Inject

class AuthorizeTvDeviceScenario @Inject internal constructor(
    private val beginSignInUseCase: BeginSignInUseCase,
    private val createDeviceAuthRequestUseCase: CreateDeviceAuthRequestUseCase,
    private val awaitDeviceApprovalUseCase: AwaitDeviceApprovalUseCase,
    private val redeemDeviceAuthRequestUseCase: RedeemDeviceAuthRequestUseCase,
    private val verificationHost: DeviceVerificationHost,
) {
    val isAvailable: Boolean get() = verificationHost.value.isNotEmpty()

    suspend operator fun invoke(
        awaitForeground: suspend () -> Unit,
        onWaiting: (DeviceAuthorization) -> Unit,
        onApproved: (DeviceAuthorization) -> Unit,
    ): AuthResult<Unit> {
        if (!isAvailable) return AuthResult.Failed(AuthFailure.ProviderRejected)
        val attempt = beginSignInUseCase() ?: return AuthResult.Failed(AuthFailure.SessionChanged)
        return when (val result = createDeviceAuthRequestUseCase(attempt, verificationHost.value)) {
            is AuthResult.Failed -> result
            is AuthResult.Success -> finish(attempt, result.value, awaitForeground, onWaiting, onApproved)
        }
    }

    private suspend fun finish(
        attempt: SessionSignInAttempt,
        request: DeviceAuthRequest,
        awaitForeground: suspend () -> Unit,
        onWaiting: (DeviceAuthorization) -> Unit,
        onApproved: (DeviceAuthorization) -> Unit,
    ): AuthResult<Unit> {
        if (Instant.now() >= request.expiresAt) return AuthResult.Failed(AuthFailure.Expired)
        onWaiting(request.display)
        return when (val result = awaitDeviceApprovalUseCase(request, awaitForeground)) {
            is AuthResult.Failed -> result
            is AuthResult.Success -> {
                onApproved(request.display)
                redeemDeviceAuthRequestUseCase(attempt, request)
            }
        }
    }
}
