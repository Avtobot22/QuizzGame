package com.quizzgame.shared.auth.domain.entity

import java.time.Instant

class GoogleChallenge(val id: String, val nonce: String, val expiresAt: Instant) {
    override fun toString(): String = "GoogleChallenge(<redacted>)"
}
