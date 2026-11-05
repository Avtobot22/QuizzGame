package com.quizzgame.core.network.result

import com.quizzgame.core.network.auth.AuthenticationRequiredException
import com.quizzgame.core.network.auth.CredentialUnavailableException
import com.quizzgame.core.network.auth.RefreshFailedException
import com.quizzgame.core.network.auth.RefreshUncertainException
import com.quizzgame.core.network.auth.SessionRevokedException
import com.quizzgame.core.network.auth.UnexpectedRefreshException
import java.io.IOException
import java.io.InterruptedIOException

internal fun classifyIoFailure(failure: IOException): NetworkFailure = when (failure) {
    is UnexpectedRefreshException      -> throw failure.original
    is RefreshUncertainException      -> NetworkFailure.RefreshUncertain(failure.reason)
    is CredentialUnavailableException -> NetworkFailure.CredentialsUnavailable(failure.kind)
    is RefreshFailedException          -> NetworkFailure.RefreshFailed(failure.reason)
    is SessionRevokedException         -> NetworkFailure.SessionRevoked
    is AuthenticationRequiredException -> NetworkFailure.AuthenticationRequired
    is InterruptedIOException          -> NetworkFailure.Timeout
    else                               -> NetworkFailure.Connection
}
