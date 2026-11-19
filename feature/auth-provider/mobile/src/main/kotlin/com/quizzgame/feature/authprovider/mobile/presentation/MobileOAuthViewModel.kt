package com.quizzgame.feature.authprovider.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.authprovider.mobile.data.ProviderCredential
import com.quizzgame.feature.authprovider.mobile.navigation.YandexLinkInbox
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import com.quizzgame.shared.auth.domain.entity.provider.ProviderSignInAttempt
import com.quizzgame.shared.auth.domain.scenario.ProviderSignInScenario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileOAuthViewModel @Inject constructor(
    private val scenario: ProviderSignInScenario,
    private val inbox: YandexLinkInbox,
) : BaseViewModel<MobileOAuthState, MobileOAuthIntent, Nothing>() {
    private var operation: Job? = null

    init {
        viewModelScope.launch {
            inbox.callback.collect { id ->
                if (id != null && inbox.take(id)) onIntent(MobileOAuthIntent.YandexReturned(id))
            }
        }
    }

    override fun createInitialState(): MobileOAuthState = MobileOAuthState.Idle

    override fun handleIntent(intent: MobileOAuthIntent) {
        when (intent) {
            is MobileOAuthIntent.Start -> start(intent.method)
            MobileOAuthIntent.Cancel -> cancel()
            is MobileOAuthIntent.GoogleReturned -> verifyGoogle(intent)
            is MobileOAuthIntent.YandexReturned -> redeemYandex(intent.attemptId)
            MobileOAuthIntent.BrowserReturned -> browserReturned()
            MobileOAuthIntent.BrowserUnavailable -> cancel(oauthFailure())
        }
    }

    private fun start(method: AuthMethod) {
        if (state.value != MobileOAuthState.Idle && state.value !is MobileOAuthState.Error) return
        run {
            scenario.start(method).map { attempt ->
                when (attempt) {
                    is ProviderSignInAttempt.Google -> MobileOAuthState.Google(attempt)
                    is ProviderSignInAttempt.Yandex -> MobileOAuthState.OpenBrowser(attempt)
                }
            }
        }
    }

    private fun verifyGoogle(intent: MobileOAuthIntent.GoogleReturned) {
        val attempt = (state.value as? MobileOAuthState.Google)?.attempt ?: return
        if (intent.challengeId != attempt.challenge.id) return
        when (val result = intent.result) {
            ProviderCredential.Cancelled -> cancel()
            ProviderCredential.Failure -> cancel(oauthFailure())
            is ProviderCredential.Token -> run {
                scenario.finishGoogle(attempt, result.value).map { MobileOAuthState.Committed }
            }
        }
    }

    fun claimBrowserLaunch(id: String): Boolean {
        val current = state.value as? MobileOAuthState.OpenBrowser ?: return false
        if (current.attempt.attempt.id != id) return false
        updateState { MobileOAuthState.AwaitingCallback(current.attempt) }
        return true
    }

    private fun browserReturned() {
        val current = state.value as? MobileOAuthState.AwaitingCallback ?: return
        val id = current.attempt.attempt.id
        if (inbox.take(id)) redeemYandex(id) else cancel()
    }

    private fun redeemYandex(id: String) {
        val attempt = (state.value as? MobileOAuthState.AwaitingCallback)?.attempt ?: return
        if (id != attempt.attempt.id) return
        run { scenario.finishYandex(attempt).map { MobileOAuthState.Committed } }
    }

    private fun cancel(next: MobileOAuthState = MobileOAuthState.Idle) {
        operation?.cancel()
        updateState { next }
    }

    private fun run(block: suspend () -> MobileOAuthState) {
        operation = viewModelScope.launch {
            updateState { MobileOAuthState.Busy }
            val next = block()
            updateState { next }
        }
    }
}

private inline fun <T> AuthResult<T>.map(success: (T) -> MobileOAuthState): MobileOAuthState = when (this) {
    is AuthResult.Success -> success(value)
    is AuthResult.Failed -> MobileOAuthState.Error(failure)
}
