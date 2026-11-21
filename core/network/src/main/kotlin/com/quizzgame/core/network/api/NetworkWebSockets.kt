package com.quizzgame.core.network.api

import com.quizzgame.core.network.auth.RequestPolicy
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.Closeable
import java.util.concurrent.TimeUnit

class NetworkWebSockets(client: OkHttpClient, private val origin: HttpUrl) : Closeable {
    private val client = client.newBuilder().pingInterval(PING_SECONDS, TimeUnit.SECONDS).build()
    private val sockets = mutableSetOf<WebSocket>()
    private var closed = false

    @Synchronized
    fun open(path: String, listener: WebSocketListener): WebSocket? {
        if (closed) return null
        require(path.startsWith("/") && !path.startsWith("//"))
        val url = requireNotNull(origin.resolve(path))
        require(url.host == origin.host && url.port == origin.port && url.scheme == origin.scheme)
        require(url.query == null && url.fragment == null && url.username.isEmpty() && url.password.isEmpty())
        val request = Request.Builder().url(url).tag(RequestPolicy::class.java, RequestPolicy()).build()
        return client.newWebSocket(request, TrackingListener(listener)).also(sockets::add)
    }

    @Synchronized
    private fun remove(socket: WebSocket) {
        sockets.remove(socket)
    }

    @Synchronized
    override fun close() {
        closed = true
        sockets.forEach(WebSocket::cancel)
        sockets.clear()
    }

    private inner class TrackingListener(private val delegate: WebSocketListener) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) = delegate.onOpen(webSocket, response)

        override fun onMessage(webSocket: WebSocket, text: String) = delegate.onMessage(webSocket, text)

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) =
            delegate.onClosing(webSocket, code, reason)

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            remove(webSocket)
            delegate.onClosed(webSocket, code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            remove(webSocket)
            delegate.onFailure(webSocket, t, response)
        }
    }

    private companion object {
        const val PING_SECONDS = 20L
    }
}
