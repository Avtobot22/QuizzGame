package com.quizzgame.core.session.api

import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.RefreshFailureReason
import java.util.concurrent.CancellationException

internal fun NetworkFailure.toRefreshFailure(cancelled: Boolean): RefreshFailure = when (this) {
    is NetworkFailure.Http              -> RefreshFailure.Http(status, backend)
    is NetworkFailure.MockConfiguration -> RefreshFailure.Diagnostic(this)
    NetworkFailure.Connection          -> RefreshFailure.Io
    NetworkFailure.Timeout             -> {
        if (cancelled) throw CancellationException("Refresh request was cancelled")
        RefreshFailure.Timeout
    }
    NetworkFailure.InvalidResponse     -> RefreshFailure.InvalidResponse
    NetworkFailure.AuthenticationRequired,
    NetworkFailure.SessionRevoked,
    is NetworkFailure.RefreshFailed,
    -> error("Unexpected refresh transport failure")
}

internal fun RefreshFailure.toRefreshFailureReason(): RefreshFailureReason = when (this) {
    is RefreshFailure.Http         -> RefreshFailureReason.Http(status, backend)
    is RefreshFailure.Diagnostic   -> RefreshFailureReason.Diagnostic(failure)
    RefreshFailure.Io              -> RefreshFailureReason.Io
    RefreshFailure.Timeout         -> RefreshFailureReason.Timeout
    RefreshFailure.InvalidResponse -> RefreshFailureReason.InvalidResponse
}
