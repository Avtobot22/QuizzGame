package com.quizzgame.shared.room.data.dto.snapshot

import kotlinx.serialization.Serializable

@Serializable
internal data class PolicyDto(
    val guestPlayersAllowed: Boolean,
    val allPlayersReadyRequired: Boolean,
)
