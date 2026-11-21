package com.quizzgame.shared.auth.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import java.time.Instant

/** Часы для срока действия OTP и относительных таймеров экрана. */
class OtpSignInEnvironment(
    val elapsed: () -> Long,
    val now: () -> Instant,
) {
    internal fun deadlineAfter(seconds: Long): Long {
        val current = elapsed()
        return current + seconds.coerceIn(0, (Long.MAX_VALUE - current) / MILLIS_PER_SECOND) * MILLIS_PER_SECOND
    }

    /** Сохраняет действующее ограничение, если сервер не прислал более поздний срок. */
    fun blockedUntil(current: Long, failure: AuthFailure): Long {
        val retryAfter = (failure as? AuthFailure.RateLimited)?.retryAfterSeconds ?: return current
        return maxOf(current, deadlineAfter(retryAfter))
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000
    }
}
