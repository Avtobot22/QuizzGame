package com.quizzgame.app.presentation

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import java.util.UUID

private val processEpoch = UUID.randomUUID().toString()

enum class AppEntryScreen { SPLASH, SIGN_IN }

sealed interface AppEntryState {
    data object Waiting : AppEntryState
    data class Blocked(val error: ErrorPresentationSpec) : AppEntryState
    data class Ready(
        val identity: String,
        val screen: AppEntryScreen,
        val restorationOwner: String = identity,
    ) : AppEntryState
}

internal fun appEntryState(
    session: SessionState,
    logoutRevision: Long,
    processIdentity: String = processEpoch,
): AppEntryState {
    val identity = "$processIdentity:${sessionEpoch(session)}:$logoutRevision"
    val owner = "${sessionEpoch(session)}:$logoutRevision"
    return when (session) {
        SessionState.Initializing -> AppEntryState.Waiting
        SessionState.Guest,
        is SessionState.Authenticated,
        -> AppEntryState.Ready(identity, AppEntryScreen.SPLASH, owner)

        SessionState.Expired        -> AppEntryState.Ready(identity, AppEntryScreen.SIGN_IN, owner)

        is SessionState.Blocked     -> when (session.kind) {
            CredentialUnavailableKind.REFRESH_UNCERTAIN   -> AppEntryState.Ready(
                identity,
                AppEntryScreen.SIGN_IN,
                owner,
            )
            CredentialUnavailableKind.STORAGE_UNAVAILABLE -> blocked(AppFailure.Storage(StorageKind.UNKNOWN))
            else                                         -> blocked(AppFailure.OperationInterrupted)
        }

        is SessionState.Unavailable -> blocked(DefaultErrorClassifier().fromSession(session))
        SessionState.Closed         -> blocked(AppFailure.OperationInterrupted)
    }
}

private fun blocked(failure: AppFailure): AppEntryState.Blocked = AppEntryState.Blocked(
    DefaultErrorDecisionPolicy().decide(
        failure,
        ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.FORBIDDEN),
    ),
)

private fun sessionEpoch(session: SessionState): String = when (session) {
    is SessionState.Authenticated -> "authenticated:${session.sessionGeneration}"
    SessionState.Guest            -> "guest"
    SessionState.Expired          -> "expired"
    SessionState.Initializing     -> "initializing"
    SessionState.Closed           -> "closed"
    is SessionState.Unavailable   -> "unavailable"
    is SessionState.Blocked       -> "blocked:${session.kind}"
}
