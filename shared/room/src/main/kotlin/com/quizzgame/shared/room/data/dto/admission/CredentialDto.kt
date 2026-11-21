package com.quizzgame.shared.room.data.dto.admission

import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialDto(
    val role: String,
    val roomToken: String,
    val expiresAt: String,
) {
    override fun toString(): String = "CredentialDto(<redacted>)"
}
