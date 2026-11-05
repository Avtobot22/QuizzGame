package com.quizzgame.core.error.policy

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.presentation.ErrorCopyKey

internal fun failurePresentation(failure: AppFailure, safety: RetrySafety): FailurePresentation = when (failure) {
    is AppFailure.Connectivity   -> failure.presentation(safety)
    is AppFailure.Service        -> failure.presentation(safety)
    is AppFailure.Authentication -> failure.presentation()
    AppFailure.InvalidServerData -> FailurePresentation(ErrorCopyKey.UNEXPECTED_RESPONSE)
    is AppFailure.Storage        -> FailurePresentation(ErrorCopyKey.STORAGE_PROBLEM)
}
