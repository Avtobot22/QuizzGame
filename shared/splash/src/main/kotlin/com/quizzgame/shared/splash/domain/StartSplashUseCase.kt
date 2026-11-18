package com.quizzgame.shared.splash.domain

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.splash.domain.entity.SplashResult
import javax.inject.Inject

class StartSplashUseCase @Inject constructor(
    private val repository: SplashRepository,
) {
    suspend operator fun invoke(): SplashResult {
        val initial = repository.currentSession()
        return terminal(initial) ?: prepareStartup(initial)
    }

    private suspend fun prepareStartup(initial: SessionState): SplashResult {
        val failure = repository.loadLaunch() ?: repository.refreshFlags()
        return if (failure == null) finishStartup(initial) else resolvedFailure(failure)
    }

    private suspend fun finishStartup(initial: SessionState): SplashResult {
        val current = repository.currentSession()
        val sessionOutcome = terminal(current)
        return when {
            sessionOutcome != null                -> sessionOutcome
            current != initial                    -> SplashResult.Failed(AppFailure.OperationInterrupted)
            current == SessionState.Guest         -> finishGuestStartup()
            current is SessionState.Authenticated -> finishAuthenticatedStartup(current.sessionGeneration)
            else                                  -> SplashResult.Failed(AppFailure.OperationInterrupted)
        }
    }

    private fun finishGuestStartup(): SplashResult {
        repository.clearUser()
        return if (repository.guestAllowed()) SplashResult.Continue else SplashResult.SignIn
    }

    private suspend fun finishAuthenticatedStartup(generation: Long): SplashResult {
        repository.loadUser(generation)
        val current = repository.currentSession()
        return terminal(current) ?: if (current == SessionState.Authenticated(generation)) {
            SplashResult.Continue
        } else {
            SplashResult.Failed(AppFailure.OperationInterrupted)
        }
    }

    private suspend fun resolvedFailure(failure: AppFailure): SplashResult =
        terminal(repository.currentSession()) ?: SplashResult.Failed(failure)

    private fun terminal(state: SessionState): SplashResult? = when (state) {
        SessionState.Expired        -> SplashResult.Failed(AppFailure.OperationInterrupted)

        is SessionState.Blocked     -> when (state.kind) {
            CredentialUnavailableKind.REFRESH_UNCERTAIN   -> SplashResult.SignIn
            CredentialUnavailableKind.STORAGE_UNAVAILABLE ->
                SplashResult.Failed(AppFailure.Storage(StorageKind.UNKNOWN))

            else                                         -> SplashResult.Failed(AppFailure.OperationInterrupted)
        }

        is SessionState.Unavailable -> SplashResult.Failed(DefaultErrorClassifier().fromSession(state))
        SessionState.Closed,
        SessionState.Initializing,
        -> SplashResult.Failed(AppFailure.OperationInterrupted)

        else                        -> null
    }
}
