package com.quizzgame.shared.auth.domain.usecase

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthRequest
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthStatus
import com.quizzgame.shared.auth.domain.repository.DeviceAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class AwaitDeviceApprovalUseCase @Inject constructor(private val repository: DeviceAuthRepository) {
    suspend operator fun invoke(
        request: DeviceAuthRequest,
        awaitForeground: suspend () -> Unit,
    ): AuthResult<Unit> {
        val remaining = Duration.between(Instant.now(), request.expiresAt).toKotlinBoundedMillis()
        if (remaining <= 0) return AuthResult.Failed(AuthFailure.Expired)
        return withTimeoutOrNull(remaining.milliseconds) {
            pollForApproval(request, awaitForeground)?.let { AuthResult.Failed(it) } ?: AuthResult.Success(Unit)
        } ?: AuthResult.Failed(AuthFailure.Expired)
    }

    private suspend fun pollForApproval(
        request: DeviceAuthRequest,
        awaitForeground: suspend () -> Unit,
    ): AuthFailure? {
        var interval = request.pollIntervalSeconds
        var result: AuthResult<DeviceAuthStatus>
        do {
            delay(interval.seconds)
            awaitForeground()
            result = repository.deviceAuthStatus(request)
            val limited = (result as? AuthResult.Failed)?.failure as? AuthFailure.RateLimited
            interval = maxOf(request.pollIntervalSeconds, limited?.retryAfterSeconds ?: 0)
        } while (limited != null || (result as? AuthResult.Success)?.value == DeviceAuthStatus.PENDING)
        return when (result) {
            is AuthResult.Failed  -> result.failure
            is AuthResult.Success -> when (result.value) {
                DeviceAuthStatus.APPROVED                         -> {
                    awaitForeground()
                    null
                }

                DeviceAuthStatus.EXPIRED                          -> AuthFailure.Expired
                DeviceAuthStatus.DENIED, DeviceAuthStatus.PENDING -> AuthFailure.ProviderRejected
            }
        }
    }
}

private fun Duration.toKotlinBoundedMillis(): Long =
    if (isNegative) 0 else runCatching { toMillis() }.getOrDefault(Long.MAX_VALUE)
