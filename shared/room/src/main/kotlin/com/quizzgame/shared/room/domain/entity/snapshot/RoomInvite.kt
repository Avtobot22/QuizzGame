package com.quizzgame.shared.room.domain.entity.snapshot

import java.time.Instant

data class RoomInvite(val code: String, val url: String?, val expiresAt: Instant)
