package com.quizzgame.shared.auth.data.mapper

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.net.HttpURLConnection.HTTP_GONE
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE

internal fun NetworkFailure.toAuthFailure(): AuthFailure {
    if (this is NetworkFailure.Http) {
        return when (status to backend?.reasonCode) {
            UNPROCESSABLE to OTP_REJECTED -> AuthFailure.InvalidCode
            HTTP_GONE to OTP_EXPIRED, HTTP_GONE to OAUTH_EXPIRED -> AuthFailure.Expired
            TOO_MANY_REQUESTS to OTP_RATE_LIMITED -> AuthFailure.RateLimited(retryAfterSeconds)
            HTTP_BAD_REQUEST to OTP_INVALID_IDENTIFIER -> AuthFailure.InvalidIdentifier
            HTTP_CONFLICT to OTP_CONSUMED, HTTP_CONFLICT to OAUTH_INVALID_ATTEMPT -> AuthFailure.Consumed
            HTTP_UNAUTHORIZED to GOOGLE_REJECTED, UNPROCESSABLE to GOOGLE_REJECTED -> AuthFailure.ProviderRejected
            HTTP_UNAVAILABLE to OAUTH_UNAVAILABLE -> AuthFailure.ProviderUnavailable
            else -> AuthFailure.General(DefaultErrorClassifier().fromNetwork(this))
        }
    }
    return AuthFailure.General(DefaultErrorClassifier().fromNetwork(this))
}

private const val UNPROCESSABLE = 422
private const val TOO_MANY_REQUESTS = 429
private const val OTP_REJECTED = 22001
private const val OTP_EXPIRED = 22002
private const val OTP_RATE_LIMITED = 22003
private const val OTP_INVALID_IDENTIFIER = 22004
private const val OTP_CONSUMED = 22005
private const val GOOGLE_REJECTED = 22006
private const val OAUTH_INVALID_ATTEMPT = 22007
private const val OAUTH_EXPIRED = 22008
private const val OAUTH_UNAVAILABLE = 22009
