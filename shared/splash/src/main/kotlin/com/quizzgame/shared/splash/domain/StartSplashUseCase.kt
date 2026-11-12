package com.quizzgame.shared.splash.domain

import com.quizzgame.core.error.classification.DefaultErrorClassifier
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.splash.domain.entity.SplashOutcome
import javax.inject.Inject

class StartSplashUseCase @Inject constructor(
    private val repository: SplashRepository,
) {
    suspend operator fun invoke(): SplashOutcome {
        val initial = repository.currentSession()
        return terminal(initial) ?: prepareStartup(initial)
    }

    private suspend fun prepareStartup(initial: SessionState): SplashOutcome {
        val failure = repository.loadLaunch() ?: repository.refreshFlags()
        return if (failure == null) finishStartup(initial) else resolvedFailure(failure)
    }

    private suspend fun finishStartup(initial: SessionState): SplashOutcome {
        val current = repository.currentSession()
        val sessionOutcome = terminal(current)
        return when {
            sessionOutcome != null                -> sessionOutcome
            current != initial                    -> SplashOutcome.Failed(AppFailure.OperationInterrupted)
            current == SessionState.Guest         -> finishGuestStartup()
            current is SessionState.Authenticated -> finishAuthenticatedStartup(current.sessionGeneration)
            else                                  -> SplashOutcome.Failed(AppFailure.OperationInterrupted)
        }
    }

    private fun finishGuestStartup(): SplashOutcome {
        repository.clearUser()
        return if (repository.guestAllowed()) SplashOutcome.Continue else SplashOutcome.SignIn
    }

    private suspend fun finishAuthenticatedStartup(generation: Long): SplashOutcome {
        val failure = repository.loadUser(generation)
        return if (failure == null) SplashOutcome.Continue else resolvedFailure(failure)
    }

    private suspend fun resolvedFailure(failure: AppFailure): SplashOutcome =
        terminal(repository.currentSession()) ?: SplashOutcome.Failed(failure)

    private fun terminal(state: SessionState): SplashOutcome? = when (state) {
        SessionState.Expired        -> SplashOutcome.SignIn

        is SessionState.Blocked     -> when (state.kind) {
            CredentialUnavailableKind.REFRESH_UNCERTAIN   -> SplashOutcome.SignIn
            CredentialUnavailableKind.STORAGE_UNAVAILABLE ->
                SplashOutcome.Failed(AppFailure.Storage(StorageKind.UNKNOWN))

            else                                         -> SplashOutcome.Failed(AppFailure.OperationInterrupted)
        }

        is SessionState.Unavailable -> SplashOutcome.Failed(DefaultErrorClassifier().fromSession(state))
        SessionState.Closed,
        SessionState.Initializing,
        -> SplashOutcome.Failed(AppFailure.OperationInterrupted)

        else                        -> null
    }
}
