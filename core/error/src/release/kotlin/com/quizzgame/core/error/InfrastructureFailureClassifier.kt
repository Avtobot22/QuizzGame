package com.quizzgame.core.error

import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.RefreshFailureReason

internal fun classifyNetworkFailure(failure: NetworkFailure): AppFailure = when (failure) {
    is NetworkFailure.Http                -> if (failure.status == HTTP_UNAUTHORIZED) {
        AppFailure.Authentication(AuthenticationKind.REQUIRED, failure.status, failure.backend?.reasonCode)
    } else {
        classifyServiceFailure(failure.status, failure.backend?.reasonCode)
    }

    NetworkFailure.Connection             -> AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE)
    NetworkFailure.Timeout                -> AppFailure.Connectivity(ConnectivityKind.TIMEOUT)
    NetworkFailure.InvalidResponse        -> AppFailure.InvalidServerData
    NetworkFailure.AuthenticationRequired -> AppFailure.Authentication(AuthenticationKind.REQUIRED)
    NetworkFailure.SessionRevoked         -> AppFailure.Authentication(AuthenticationKind.REVOKED)
    is NetworkFailure.RefreshFailed       -> classifyRefreshFailure(failure.reason)
}

private fun classifyRefreshFailure(reason: RefreshFailureReason): AppFailure = when (reason) {
    is RefreshFailureReason.Http         -> classifyServiceFailure(reason.status, reason.backend?.reasonCode)
    RefreshFailureReason.Io              -> AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE)
    RefreshFailureReason.Timeout         -> AppFailure.Connectivity(ConnectivityKind.TIMEOUT)
    RefreshFailureReason.InvalidResponse -> AppFailure.InvalidServerData
    RefreshFailureReason.Storage         -> AppFailure.Storage(StorageKind.UNKNOWN)
}

private const val HTTP_UNAUTHORIZED = 401
