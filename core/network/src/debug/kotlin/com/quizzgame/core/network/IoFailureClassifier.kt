package com.quizzgame.core.network

import java.io.IOException
import java.io.InterruptedIOException

internal fun classifyIoFailure(failure: IOException): NetworkFailure = when (failure) {
    is NetworkDiagnosticException      -> failure.diagnostic
    is UnexpectedRefreshException      -> throw failure.original
    is RefreshFailedException          -> NetworkFailure.RefreshFailed(failure.reason)
    is SessionRevokedException         -> NetworkFailure.SessionRevoked
    is AuthenticationRequiredException -> NetworkFailure.AuthenticationRequired
    is InterruptedIOException          -> NetworkFailure.Timeout
    else                               -> NetworkFailure.Connection
}
