package com.quizzgame.core.network.auth

import com.quizzgame.core.network.http.HttpHeaderNames
import com.quizzgame.core.network.http.HttpHeaderValues
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthenticationRequiredException : IOException("Authentication is required")

internal class AccessTokenInterceptor(
    private val credentialsProvider: CredentialsProvider,
    private val calls: AuthCallRegistry? = null,
) : Interceptor {

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
            val admitted = calls?.context(chain.call())
            if (admitted != null && admitted.observedCredentials.sessionGeneration != snapshot.sessionGeneration) {
                throw AuthenticationRequiredException()
            }
            if (admitted != null && !admitted.observedCredentials.sameVersion(snapshot)) {
                request.tag(AuthFollowUp::class.java, AuthFollowUp())
            }
            request.tag(RequestAuthContext::class.java, RequestAuthContext(snapshot, admitted?.refreshAttempt))
            if (access != null) request.header(HttpHeaderNames.AUTHORIZATION, HttpHeaderValues.BEARER_PREFIX + access)
        }
        val outgoing = request.build()
        val response = chain.proceed(outgoing)
        val observed = outgoing.tag(RequestAuthContext::class.java)?.observedCredentials
        if (observed != null) {
            try {
                if (credentialsProvider.snapshot().sessionGeneration != observed.sessionGeneration) {
                    throw AuthenticationRequiredException()
                }
            } catch (failure: IOException) {
                response.close()
                throw failure
            }
        }
        return response
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
