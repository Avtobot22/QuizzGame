package com.quizzgame.core.network

import okhttp3.Call
import okhttp3.EventListener
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

internal class AuthCallRegistry(
    private val credentialsProvider: CredentialsProvider,
    private val coordinator: RefreshCoordinator,
) : EventListener.Factory {

    private val active = ConcurrentHashMap<Call, AuthCallContext>()

    override fun create(call: Call): EventListener {
        val context = AuthCallContext()
        return object : EventListener() {
            override fun callStart(call: Call) {
                active[call] = context
                val request = call.request()
                val policy = request.tag(RequestPolicy::class.java)?.forRequest(request)
                if (policy?.replay == ReplayPolicy.AFTER_REFRESH_ONCE) {
                    try {
                        val snapshot = credentialsProvider.snapshot()
                        val attempt = if (snapshot.accessToken != null) coordinator.register(snapshot) else null
                        context.attach(RequestAuthContext(snapshot, attempt))
                    } catch (_: IOException) {
                        context.unavailable = true
                    }
                }
            }

            override fun canceled(call: Call) = context.cancel()
            override fun callEnd(call: Call) {
                active.remove(call)?.close()
            }

            override fun callFailed(call: Call, ioe: IOException) {
                active.remove(call)?.close()
            }
        }
    }

    fun context(call: Call): RequestAuthContext? {
        val context = active[call]
        if (context?.unavailable == true) throw IOException("Session credentials were unavailable at call start")
        return context?.auth
    }
}

private class AuthCallContext {

    private var cancelled = false
    private var finished = false

    @Volatile
    var unavailable = false

    @Volatile
    var auth: RequestAuthContext? = null

    fun attach(value: RequestAuthContext) {
        val state = synchronized(this) {
            auth = value
            cancelled to finished
        }
        if (state.second) value.refreshAttempt?.close() else if (state.first) value.refreshAttempt?.cancel()
    }

    fun cancel() {
        val value = synchronized(this) {
            cancelled = true
            auth?.refreshAttempt
        }
        value?.cancel()
    }

    fun close() {
        val value = synchronized(this) {
            finished = true
            auth?.refreshAttempt.also { auth = null }
        }
        value?.close()
    }
}
