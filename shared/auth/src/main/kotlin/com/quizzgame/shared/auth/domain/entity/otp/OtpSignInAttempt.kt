package com.quizzgame.shared.auth.domain.entity.otp

import com.quizzgame.core.session.lifecycle.SessionSignInAttempt
import java.util.UUID

class OtpSignInAttempt internal constructor(
    val challenge: OtpChallenge,
    internal val session: SessionSignInAttempt,
) {
    private var verification: Verification? = null

    fun forgetVerification() {
        verification = null
    }

    internal fun keyFor(code: String): String {
        val current = verification?.takeIf { it.code == code }
            ?: Verification(code, UUID.randomUUID().toString()).also { verification = it }
        return current.key
    }

    private class Verification(val code: String, val key: String)
}
