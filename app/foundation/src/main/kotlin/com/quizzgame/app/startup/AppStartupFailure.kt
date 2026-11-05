package com.quizzgame.app.startup

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageFailure

sealed interface AppStartupFailure {
    data class Storage(val reason: SessionStorageFailure) : AppStartupFailure
    data object NetworkConfiguration : AppStartupFailure
    data object Transport : AppStartupFailure
}

internal fun startupFailureState(
    failure: AppStartupFailure,
    recovery: StorageRecoveryPolicy,
): AppStartupState.Failed {
    val error = when (failure) {
        is AppStartupFailure.Storage -> {
            val classified = DefaultErrorClassifier().fromSession(SessionState.Unavailable(failure.reason))
            DefaultErrorDecisionPolicy().decide(
                classified,
                ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.FORBIDDEN),
            ).copy(
                actions = if (
                    recovery.decide(failure.reason) != StorageRecoveryDecision.UnavailableWithoutAutomaticRecovery
                ) {
                    listOf(ErrorActionKind.RETRY)
                } else {
                    emptyList()
                },
            )
        }
        AppStartupFailure.NetworkConfiguration ->
            ErrorPresentationSpec(ErrorSurface.BLOCKING, ErrorCopyKey.GENERIC_FAILURE)
        AppStartupFailure.Transport -> ErrorPresentationSpec(
            ErrorSurface.BLOCKING,
            ErrorCopyKey.GENERIC_FAILURE,
            actions = listOf(ErrorActionKind.RETRY),
        )
    }
    return AppStartupState.Failed(error, failure)
}

internal fun SessionState.startupState(recovery: StorageRecoveryPolicy): AppStartupState = when (this) {
    is SessionState.Ready, is SessionState.Blocked -> AppStartupState.Ready
    is SessionState.Unavailable -> startupFailureState(AppStartupFailure.Storage(reason), recovery)
    SessionState.Closed -> AppStartupState.Closed
    SessionState.Initializing -> error("Session factory returned an uninitialized manager")
}
