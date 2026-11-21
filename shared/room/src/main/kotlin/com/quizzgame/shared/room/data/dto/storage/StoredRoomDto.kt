package com.quizzgame.shared.room.data.dto.storage

import com.quizzgame.shared.room.data.dto.admission.CredentialDto
import kotlinx.serialization.Serializable

@Serializable
internal data class StoredRoomDto(
    val roomId: String,
    val credential: CredentialDto,
    val self: String?,
    val accountGeneration: Long?,
) {
    override fun toString(): String = "StoredRoomDto(<redacted>)"
}
