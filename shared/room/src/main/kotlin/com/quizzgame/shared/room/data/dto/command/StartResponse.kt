package com.quizzgame.shared.room.data.dto.command

import kotlinx.serialization.Serializable

@Serializable
internal data class StartResponse(
    val roomId: String,
    val state: String,
    val version: Long,
)
