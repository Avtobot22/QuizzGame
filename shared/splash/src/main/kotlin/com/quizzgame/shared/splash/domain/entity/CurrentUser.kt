package com.quizzgame.shared.splash.domain.entity

import java.time.Instant
import java.util.UUID

data class CurrentUser(
    val id: UUID,
    val createdAt: Instant,
    val email: String?,
    val phoneNumber: String?,
    val displayName: String?,
    val avatarUrl: String?,
    val roles: List<String>,
) {
    override fun toString(): String = "CurrentUser(<redacted>)"
}
