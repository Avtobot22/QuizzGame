package com.quizzgame.shared.room.domain.entity.session

import java.util.UUID

@JvmInline
value class RoomId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun parse(value: String): RoomId? = value.roomUuid()?.let(::RoomId)
    }
}

@JvmInline
value class ParticipantId(val value: UUID) {
    override fun toString(): String = value.toString()
}

internal fun String.roomUuid(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
    ?.takeIf { it.toString().equals(this, ignoreCase = true) }
