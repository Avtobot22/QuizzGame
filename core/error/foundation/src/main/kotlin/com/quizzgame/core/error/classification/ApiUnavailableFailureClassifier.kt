package com.quizzgame.core.error.classification

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.network.api.ApiUnavailableReason

fun classifyApiUnavailable(reason: ApiUnavailableReason): AppFailure = when (reason) {
    ApiUnavailableReason.STORAGE       -> AppFailure.Storage(StorageKind.UNKNOWN)
    ApiUnavailableReason.CONFIGURATION -> AppFailure.Configuration
    ApiUnavailableReason.TRANSPORT     -> AppFailure.OperationInterrupted
}
