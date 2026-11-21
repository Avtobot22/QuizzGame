package com.quizzgame.shared.account.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNicknameRequest(val displayName: String) {
    override fun toString(): String = "UpdateNicknameRequest(<redacted>)"
}
