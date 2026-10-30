package com.quizzgame.core.network

import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

internal class AuthFollowUp

internal class SessionAuthenticator(private val credentialsProvider: CredentialsProvider) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        val context = eligibleContext(request) ?: return null
        val attempt = checkNotNull(context.refreshAttempt)
        response.body?.close()
        return when (val outcome = attempt.refresh()) {
            is RefreshOutcome.Ready           -> followUp(request, context, outcome.credentials)
            is RefreshOutcome.Failed          -> throw RefreshFailedException(outcome.reason)
            is RefreshOutcome.Unexpected      -> throw UnexpectedRefreshException(outcome.exception)
            RefreshOutcome.Revoked            -> throw SessionRevokedException()
            RefreshOutcome.SessionUnavailable -> throw AuthenticationRequiredException()
            RefreshOutcome.Cancelled          -> throw IOException("API request was cancelled")
        }
    }

    private fun eligibleContext(request: Request): RequestAuthContext? {
        val policy = request.tag(RequestPolicy::class.java)?.forRequest(request)
        if (policy?.replay != ReplayPolicy.AFTER_REFRESH_ONCE || request.tag(AuthFollowUp::class.java) != null) {
            return null
        }
        return request.tag(RequestAuthContext::class.java)?.takeIf { it.refreshAttempt != null }
    }

    private fun followUp(request: Request, context: RequestAuthContext, updated: CredentialsSnapshot): Request {
        if (!credentialsProvider.snapshot().sameVersion(updated)) throw AuthenticationRequiredException()
        val access = updated.accessToken ?: throw AuthenticationRequiredException()
        return request.newBuilder()
            .header(HttpHeaderNames.AUTHORIZATION, HttpHeaderValues.BEARER_PREFIX + access)
            .tag(AuthFollowUp::class.java, AuthFollowUp())
            .tag(RequestAuthContext::class.java, RequestAuthContext(updated, context.refreshAttempt))
            .build()
    }
}

internal class AuthReplayGuardInterceptor(private val credentialsProvider: CredentialsProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.tag(AuthFollowUp::class.java) != null) {
            val observed = request.tag(RequestAuthContext::class.java)?.observedCredentials
            if (observed == null || !credentialsProvider.snapshot().sameVersion(observed)) {
                throw AuthenticationRequiredException()
            }
        }
        return chain.proceed(request)
    }
}
