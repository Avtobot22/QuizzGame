package com.quizzgame.shared.room.data.dto.snapshot

import kotlinx.serialization.Serializable

@Serializable
internal data class ParticipantDto(
    val participantId: String,
    val displayName: String,
    val ready: Boolean,
    val connection: String,
) {
    override fun toString(): String = "ParticipantDto(<redacted>)"
}
