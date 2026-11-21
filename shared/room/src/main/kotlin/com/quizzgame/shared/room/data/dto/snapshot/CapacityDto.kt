package com.quizzgame.shared.room.data.dto.snapshot

import kotlinx.serialization.Serializable

@Serializable
internal data class CapacityDto(
    val minPlayers: Int,
    val maxPlayers: Int,
)
