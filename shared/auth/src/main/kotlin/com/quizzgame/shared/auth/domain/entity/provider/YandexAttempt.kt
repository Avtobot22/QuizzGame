package com.quizzgame.shared.auth.domain.entity.provider

import java.net.URI
import java.time.Instant

class YandexAttempt(val id: String, val url: URI, val secret: String, val expiresAt: Instant) {
    override fun toString(): String = "YandexAttempt(<redacted>)"
}
