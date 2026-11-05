package com.quizzgame.core.session.refresh.api

import com.quizzgame.core.network.auth.RefreshFailureReason
import com.quizzgame.core.network.result.NetworkFailure
import java.util.concurrent.CancellationException

internal fun NetworkFailure.toRefreshFailure(cancelled: Boolean): RefreshFailure = when (this) {
    is NetworkFailure.Http              -> RefreshFailure.Http(status, backend)
    NetworkFailure.Connection          -> RefreshFailure.Io
    NetworkFailure.Timeout             -> {
        if (cancelled) throw CancellationException("Refresh request was cancelled")
        RefreshFailure.Timeout
    }
    NetworkFailure.InvalidResponse     -> RefreshFailure.InvalidResponse
    NetworkFailure.AuthenticationRequired,
    NetworkFailure.SessionRevoked,
    is NetworkFailure.RefreshFailed,
    is NetworkFailure.RefreshUncertain,
    is NetworkFailure.CredentialsUnavailable,
    -> error("Unexpected refresh transport failure")
}

internal fun RefreshFailure.toRefreshFailureReason(): RefreshFailureReason = when (this) {
    is RefreshFailure.Http         -> RefreshFailureReason.Http(status, backend)
    RefreshFailure.Io              -> RefreshFailureReason.Io
    RefreshFailure.Timeout         -> RefreshFailureReason.Timeout
    RefreshFailure.InvalidResponse -> RefreshFailureReason.InvalidResponse
}

internal fun RefreshFailure.isKnownRejected(): Boolean = (
    this is RefreshFailure.Http && status == HTTP_BAD_REQUEST && backend?.reasonCode == MALFORMED_REFRESH_CODE
    )

private const val HTTP_BAD_REQUEST = 400
private const val MALFORMED_REFRESH_CODE = 21008
