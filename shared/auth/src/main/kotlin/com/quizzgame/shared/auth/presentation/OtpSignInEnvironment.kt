package com.quizzgame.shared.auth.presentation

import com.quizzgame.shared.auth.domain.entity.AuthSurface
import java.time.Instant

class OtpSignInEnvironment(
    val surface: AuthSurface,
    val elapsed: () -> Long,
    val now: () -> Instant,
) {
    internal fun deadlineAfter(seconds: Long): Long {
        val current = elapsed()
        return current + seconds.coerceIn(0, (Long.MAX_VALUE - current) / MILLIS_PER_SECOND) * MILLIS_PER_SECOND
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000
    }
}
