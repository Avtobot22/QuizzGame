package com.quizzgame.shared.room.data.storage

import com.quizzgame.shared.room.domain.entity.session.RoomRole
import java.time.Instant

internal class RoomCredential(val role: RoomRole, val token: String, val expiresAt: Instant) {
    override fun toString(): String = "RoomCredential(<redacted>)"
}
