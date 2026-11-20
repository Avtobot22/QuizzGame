package com.quizzgame.shared.room.data.dto.stream

import kotlinx.serialization.Serializable

@Serializable
internal data class RoomWsAuth(
    val roomToken: String,
    val lastSeenVersion: Long? = null,
    val type: String,
) {
    override fun toString(): String = "RoomWsAuth(<redacted>)"
}
