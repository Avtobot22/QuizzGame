package com.quizzgame.shared.room.presentation

import androidx.annotation.StringRes
import com.quizzgame.shared.room.R
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.snapshot.RoomConnection
import com.quizzgame.shared.room.domain.entity.snapshot.RoomParticipant

@StringRes
fun RoomParticipant.statusLabel(): Int = when (connection) {
    RoomConnection.CONNECTED -> if (ready) R.string.room_ready else R.string.room_not_ready
    RoomConnection.RECONNECTING -> R.string.room_rejoining
    RoomConnection.DISCONNECTED -> R.string.room_disconnected
}

@StringRes
fun RoomFailureReason.endLabel(): Int = when (this) {
    RoomFailureReason.REMOVED -> R.string.room_removed
    RoomFailureReason.EXPIRED, RoomFailureReason.INVALID_TOKEN -> R.string.room_expired
    RoomFailureReason.ACCOUNT_CHANGED -> R.string.room_account_changed
    RoomFailureReason.CLOSED -> R.string.room_closed
    else -> R.string.room_unavailable
}

@StringRes
fun RoomFailureReason.endDescription(): Int = when (this) {
    RoomFailureReason.REMOVED -> R.string.room_removed_description
    RoomFailureReason.EXPIRED, RoomFailureReason.INVALID_TOKEN -> R.string.room_expired_description
    RoomFailureReason.ACCOUNT_CHANGED -> R.string.room_account_changed_description
    RoomFailureReason.CLOSED -> R.string.room_closed_description
    else -> R.string.room_unavailable_description
}
