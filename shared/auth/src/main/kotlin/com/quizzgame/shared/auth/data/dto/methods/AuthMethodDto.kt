package com.quizzgame.shared.auth.data.dto.methods

import kotlinx.serialization.Serializable

@Serializable
internal class AuthMethodDto(val id: String, val enabled: Boolean, val displayName: String) {
    override fun toString(): String = "AuthMethodDto(<redacted>)"
}
