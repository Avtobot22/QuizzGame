package com.quizzgame.core.network.api

import okhttp3.WebSocket
import okhttp3.WebSocketListener

fun interface ApiWebSockets {

    suspend fun open(path: String, listener: WebSocketListener): WebSocket?
}
