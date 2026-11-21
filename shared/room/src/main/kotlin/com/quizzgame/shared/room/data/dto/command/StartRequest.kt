package com.quizzgame.shared.room.data.dto.command

import kotlinx.serialization.Serializable

@Serializable
internal data class StartRequest(
    val expectedVersion: Long,
)
