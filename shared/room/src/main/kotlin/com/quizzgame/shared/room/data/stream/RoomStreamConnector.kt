package com.quizzgame.shared.room.data.stream

import com.quizzgame.core.network.api.ApiWebSockets
import com.quizzgame.shared.room.data.api.ROOM_API
import com.quizzgame.shared.room.data.dto.stream.RoomWsAuth
import com.quizzgame.shared.room.data.storage.StoredRoom
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

internal class RoomStreamConnector @Inject constructor(
    private val sockets: ApiWebSockets,
    private val json: Json,
    private val decoder: RoomStreamDecoder,
) {
    fun observe(room: StoredRoom, lastSeenVersion: Long?): Flow<RoomStreamEvent> = callbackFlow {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val auth = RoomWsAuth(room.credential.token, lastSeenVersion, type = "auth")
                if (!webSocket.send(json.encodeToString(auth))) close()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val event = decoder.decode(text, room.credential.role) ?: return
                if (!trySend(event).isSuccess) close()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, null)
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close()
            }
        }
        val socket = sockets.open("$ROOM_API/${room.id}/stream", listener)
        if (socket == null) close()
        awaitClose { socket?.cancel() }
    }
}
