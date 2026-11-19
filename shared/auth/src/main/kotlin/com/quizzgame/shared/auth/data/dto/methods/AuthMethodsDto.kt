package com.quizzgame.shared.auth.data.dto.methods

import kotlinx.serialization.Serializable

@Serializable
internal class AuthMethodsDto(val methods: List<AuthMethodDto>, val guestCatalogAvailable: Boolean) {
    override fun toString(): String = "AuthMethodsDto(<redacted>)"
}
