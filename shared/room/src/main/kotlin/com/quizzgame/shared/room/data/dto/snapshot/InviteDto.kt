package com.quizzgame.shared.room.data.dto.snapshot

import kotlinx.serialization.Serializable

@Serializable
internal data class InviteDto(
    val code: String,
    val url: String,
    val expiresAt: String,
)
