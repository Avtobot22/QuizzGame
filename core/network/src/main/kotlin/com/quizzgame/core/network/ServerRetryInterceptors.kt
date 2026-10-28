package com.quizzgame.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_UNAVAILABLE

private class ServerRetryContext {

    var originalRetryAfter: List<String>? = null
}

internal class RestoreRetryAfterInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val context = ServerRetryContext()
        val request = chain.request().newBuilder().tag(ServerRetryContext::class.java, context).build()
        val response = chain.proceed(request)
        val original = context.originalRetryAfter ?: return response
        val restored = response.newBuilder().removeHeader(HttpHeaderNames.RETRY_AFTER)
        original.forEach { restored.addHeader(HttpHeaderNames.RETRY_AFTER, it) }
        return restored.build()
    }
}

internal class PreventServerRetryInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val retryAfter = response.header(HttpHeaderNames.RETRY_AFTER)?.toLongOrNull()
        if (response.code != HTTP_UNAVAILABLE || retryAfter != HttpHeaderValues.RETRY_AFTER_IMMEDIATE_SECONDS) {
            return response
        }
        val context = checkNotNull(chain.request().tag(ServerRetryContext::class.java))
        context.originalRetryAfter = response.headers.values(HttpHeaderNames.RETRY_AFTER)
        return response.newBuilder()
            .header(HttpHeaderNames.RETRY_AFTER, HttpHeaderValues.RETRY_AFTER_DEFERRED_SECONDS.toString())
            .build()
    }
}
