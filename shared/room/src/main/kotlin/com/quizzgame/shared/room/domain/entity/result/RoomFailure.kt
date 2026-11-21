package com.quizzgame.shared.room.domain.entity.result

import com.quizzgame.core.error.failure.AppFailure

data class RoomFailure(
    val cause: AppFailure,
    val reason: RoomFailureReason? = null,
    val retryAfterSeconds: Long? = null,
    val uncertain: Boolean = false,
) {
    internal val terminal: Boolean get() = reason in TERMINAL_REASONS
}

enum class RoomFailureReason {
    HOST_AUTH_REQUIRED,
    PARTICIPANT_AUTH_REQUIRED,
    INVALID_TOKEN,
    FORBIDDEN,
    NOT_FOUND,
    FULL,
    NAME_TAKEN,
    STATE_CONFLICT,
    NOT_ENOUGH_PLAYERS,
    PLAYERS_NOT_READY,
    VERSION_CONFLICT,
    EXPIRED,
    REMOVED,
    RATE_LIMITED,
    ALREADY_IN_ROOM,
    CLOSED,
    ACCOUNT_CHANGED,
}

private val TERMINAL_REASONS = setOf(
    RoomFailureReason.INVALID_TOKEN,
    RoomFailureReason.NOT_FOUND,
    RoomFailureReason.EXPIRED,
    RoomFailureReason.REMOVED,
    RoomFailureReason.CLOSED,
    RoomFailureReason.ACCOUNT_CHANGED,
)
