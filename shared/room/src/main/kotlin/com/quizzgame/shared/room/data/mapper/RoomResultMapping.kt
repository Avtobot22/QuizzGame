package com.quizzgame.shared.room.data.mapper

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.classification.classifyApiUnavailable
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.shared.room.domain.entity.result.RoomFailure
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult

internal fun <T, R> ApiCallResult<T>.mapRoom(transform: (T) -> R): RoomResult<R> = when (this) {
    ApiCallResult.Closed -> RoomResult.Failed(RoomFailure(AppFailure.OperationInterrupted, uncertain = true))
    is ApiCallResult.Unavailable -> RoomResult.Failed(RoomFailure(classifyApiUnavailable(reason), uncertain = true))
    is ApiCallResult.Executed -> when (val response = result) {
        is NetworkResult.Success -> try {
            RoomResult.Success(transform(response.value))
        } catch (_: IllegalArgumentException) {
            RoomResult.Failed(RoomFailure(AppFailure.InvalidServerData, uncertain = true))
        }
        is NetworkResult.Failure -> RoomResult.Failed(response.reason.toRoomFailure())
    }
}

private fun NetworkFailure.toRoomFailure(): RoomFailure {
    val appFailure = DefaultErrorClassifier().fromNetwork(this)
    if (this !is NetworkFailure.Http) return RoomFailure(appFailure, uncertain = true)
    val backendCode = backend?.reasonCode
    val reason = roomReasonCodes[backendCode]
        ?: RoomFailureReason.EXPIRED.takeIf { status == HTTP_GONE }
    return RoomFailure(appFailure, reason, retryAfterSeconds, status >= HTTP_SERVER_ERROR || status == HTTP_TIMEOUT)
}

private const val HTTP_GONE = 410
private const val HTTP_TIMEOUT = 408
private const val HTTP_SERVER_ERROR = 500

private val roomReasonCodes = mapOf(
    32001 to RoomFailureReason.HOST_AUTH_REQUIRED,
    32002 to RoomFailureReason.PARTICIPANT_AUTH_REQUIRED,
    32003 to RoomFailureReason.INVALID_TOKEN,
    32004 to RoomFailureReason.FORBIDDEN,
    32005 to RoomFailureReason.NOT_FOUND,
    32006 to RoomFailureReason.FULL,
    32007 to RoomFailureReason.NAME_TAKEN,
    32008 to RoomFailureReason.STATE_CONFLICT,
    32009 to RoomFailureReason.NOT_ENOUGH_PLAYERS,
    32010 to RoomFailureReason.PLAYERS_NOT_READY,
    32011 to RoomFailureReason.VERSION_CONFLICT,
    32012 to RoomFailureReason.EXPIRED,
    32013 to RoomFailureReason.REMOVED,
    32014 to RoomFailureReason.RATE_LIMITED,
    32015 to RoomFailureReason.ALREADY_IN_ROOM,
)
