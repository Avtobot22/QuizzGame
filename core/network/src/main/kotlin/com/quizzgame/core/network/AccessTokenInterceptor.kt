package com.quizzgame.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthenticationRequiredException : IOException("Authentication is required")

internal class AccessTokenInterceptor(private val credentialsProvider: CredentialsProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val policy = (original.tag(RequestPolicy::class.java) ?: RequestPolicy()).forRequest(original)
        val request = original.newBuilder()
            .removeHeader(HttpHeaderNames.AUTHORIZATION)
            .tag(RequestPolicy::class.java, policy)
            .tag(RequestAuthContext::class.java, null)
        if (policy.auth != AuthPolicy.NONE) {
            val snapshot = credentialsProvider.snapshot()
            val access = snapshot.accessToken
            if (access == null && policy.auth == AuthPolicy.REQUIRED) throw AuthenticationRequiredException()
            request.tag(RequestAuthContext::class.java, RequestAuthContext(snapshot))
            if (access != null) request.header(HttpHeaderNames.AUTHORIZATION, HttpHeaderValues.BEARER_PREFIX + access)
        }
        return chain.proceed(request.build())
    }
}

internal class AnonymousRequestInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder()
            .removeHeader(HttpHeaderNames.AUTHORIZATION)
            .tag(RequestPolicy::class.java, RequestPolicy())
            .tag(RequestAuthContext::class.java, null)
            .build(),
    )
}
