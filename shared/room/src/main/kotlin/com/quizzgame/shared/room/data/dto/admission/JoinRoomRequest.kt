package com.quizzgame.shared.room.data.dto.admission

import kotlinx.serialization.Serializable

@Serializable
internal data class JoinRoomRequest(
    val code: String,
    val displayName: String,
    val clientInstanceId: String,
) {
    override fun toString(): String = "JoinRoomRequest(<redacted>)"
}
