package com.quizzgame.shared.room.data.storage

import com.quizzgame.shared.room.domain.entity.session.ParticipantId
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomSession

internal class StoredRoom(
    val id: RoomId,
    val credential: RoomCredential,
    val self: ParticipantId?,
    val accountGeneration: Long?,
) {
    val session = RoomSession(id, credential.role, credential.expiresAt, self, accountGeneration != null)
}
