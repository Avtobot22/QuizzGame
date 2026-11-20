package com.quizzgame.shared.room.domain.entity.admission

import java.net.URI
import javax.inject.Inject

class RoomJoinLink @Inject constructor(private val settings: RoomEndpointSettings) {
    fun codeFrom(value: String): String? {
        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        if (uri.scheme != "https" || !uri.host.equals(settings.joinHost, ignoreCase = true)) return null
        val expectedCode = uri.rawPath?.removePrefix("/r/") ?: return null
        return expectedCode.takeIf {
            uri.rawPath == "/r/$it" && RoomJoinInput.validCode(it) &&
                uri.rawQuery == null && uri.rawFragment == null && uri.rawUserInfo == null && uri.port == -1
        }
    }
}
