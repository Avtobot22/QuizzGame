package com.quizzgame.shared.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal class TokenPairDto(val access: String, val refresh: String) {
    override fun toString(): String = "TokenPairDto(<redacted>)"
}
