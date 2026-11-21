package com.quizzgame.shared.room.domain.entity.snapshot

data class RoomPermissions(val canStart: Boolean, val canClose: Boolean, val canKick: Boolean, val canReady: Boolean)
