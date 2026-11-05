package com.quizzgame.core.session.storage

import com.quizzgame.core.session.model.TokenPair
import java.util.UUID

class SessionRecord(
    val deviceId: String,
    val tokens: TokenPair?,
    val epoch: Long = 1,
    val revision: Long = 1,
    val intent: SessionIntent = SessionIntent.NONE,
) {
    init {
        val canonicalId = try {
            UUID.fromString(deviceId).toString()
        } catch (_: IllegalArgumentException) {
            null
        }
        require(canonicalId == deviceId) { "Session device ID must be a canonical UUID" }
        require(epoch > 0 && revision > 0) { "Session counters must be positive" }
    }

    override fun toString(): String = "SessionRecord(deviceId=<redacted>, tokens=<redacted>)"
}

enum class SessionIntent { NONE, REFRESH, LOGOUT, REVOKED }
