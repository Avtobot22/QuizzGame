package com.quizzgame.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class CommonHeadersInterceptor(
    private val configuration: NetworkConfiguration,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!configuration.baseUrl.accepts(chain.request().url)) {
            throw IOException("API request must use the configured origin")
        }
        val headers = configuration.headers
        val request = chain.request().newBuilder()
            .header(HttpHeaderNames.ACCEPT, HttpHeaderValues.APPLICATION_JSON)
            .header(HttpHeaderNames.X_ENDPOINT_VERSION, headers.apiVersion.toString())
            .header(HttpHeaderNames.X_CLIENT_PLATFORM, headers.clientPlatform)
            .header(HttpHeaderNames.USER_AGENT, headers.userAgent)
        return chain.proceed(request.build())
    }
}
