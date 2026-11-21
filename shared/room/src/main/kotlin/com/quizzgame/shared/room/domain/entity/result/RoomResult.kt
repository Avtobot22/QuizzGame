package com.quizzgame.shared.room.domain.entity.result

sealed interface RoomResult<out T> {
    data class Success<T>(val value: T) : RoomResult<T>
    data class Failed(val failure: RoomFailure) : RoomResult<Nothing>
}
