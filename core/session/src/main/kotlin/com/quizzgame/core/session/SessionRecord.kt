package com.quizzgame.core.session

import java.util.UUID

class SessionRecord(val deviceId: String, val tokens: TokenPair?) {
    init {
        val canonicalId = try {
            UUID.fromString(deviceId).toString()
        } catch (_: IllegalArgumentException) {
            null
        }
        require(canonicalId == deviceId) { "Session device ID must be a canonical UUID" }
    }

    override fun toString(): String = "SessionRecord(deviceId=<redacted>, tokens=<redacted>)"
}
