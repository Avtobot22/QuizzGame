package com.quizzgame.core.session.refresh.api.dto

import com.quizzgame.core.session.model.TokenPair
import kotlinx.serialization.Serializable

@Serializable
internal class RefreshResponseDto(val access: String, val refresh: String) {
    fun toTokenPair(): TokenPair = TokenPair(access, refresh)

    override fun toString(): String = "RefreshResponseDto(access=<redacted>, refresh=<redacted>)"
}
