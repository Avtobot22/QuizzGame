package com.quizzgame.shared.room.data.dto.command

import kotlinx.serialization.Serializable

@Serializable
internal data class ReadyRequest(
    val ready: Boolean,
)
