package com.quizzgame.feature.auth.mobile.oauth

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.auth.domain.AuthSessionAttempt
import com.quizzgame.shared.auth.domain.AuthUseCase
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

internal sealed interface MobileOAuthIntent {
    data class Start(val method: AuthMethod) : MobileOAuthIntent
    data object Cancel : MobileOAuthIntent
    data class GoogleReturned(val challengeId: String, val result: ProviderCredential) : MobileOAuthIntent
    data class YandexReturned(val attemptId: String) : MobileOAuthIntent
    data object BrowserReturned : MobileOAuthIntent
    data object BrowserUnavailable : MobileOAuthIntent
}

@HiltViewModel
internal class MobileOAuthViewModel @Inject constructor(
    private val auth: AuthUseCase,
    private val inbox: YandexLinkInbox,
) : BaseViewModel<MobileOAuthState, MobileOAuthIntent, Nothing>() {
    private var session: AuthSessionAttempt? = null
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
            val attempt = auth.begin()
            if (attempt == null) {
                MobileOAuthState.Error(AuthFailure.SessionChanged)
            } else {
                session = attempt
                when (method) {
                    AuthMethod.GOOGLE -> auth.googleChallenge().map(MobileOAuthState::Google)
                    AuthMethod.YANDEX -> auth.yandexAttempt(attempt).map(MobileOAuthState::Yandex)
                    else -> oauthFailure()
                }
            }
        }
    }

    private fun verifyGoogle(intent: MobileOAuthIntent.GoogleReturned) {
        val challenge = (state.value as? MobileOAuthState.Google)?.challenge ?: return
        val attempt = session ?: return
        if (intent.challengeId != challenge.id) return
        when (val result = intent.result) {
            ProviderCredential.Cancelled -> cancel()
            ProviderCredential.Failure -> cancel(oauthFailure())
            is ProviderCredential.Token -> run {
                if (Instant.now() >= challenge.expiresAt) {
                    MobileOAuthState.Error(AuthFailure.Expired)
                } else {
                    auth.verifyGoogle(attempt, challenge.id, result.value).map { MobileOAuthState.Committed }
                }
            }
        }
    }

    fun claimBrowserLaunch(id: String): Boolean {
        val current = state.value as? MobileOAuthState.Yandex ?: return false
        if (current.attempt.id != id || current.browserOpened) return false
        updateState { current.copy(browserOpened = true) }
        return true
    }

    private fun browserReturned() {
        val current = state.value as? MobileOAuthState.Yandex ?: return
        if (!current.browserOpened) return
        val id = current.attempt.id
        if (inbox.take(id)) redeemYandex(id) else cancel()
    }

    private fun redeemYandex(id: String) {
        val oauth = (state.value as? MobileOAuthState.Yandex)?.attempt ?: return
        val attempt = session ?: return
        if (id != oauth.id) return
        run {
            if (Instant.now() >= oauth.expiresAt) {
                MobileOAuthState.Error(AuthFailure.Expired)
            } else {
                auth.redeemYandex(attempt, oauth).map { MobileOAuthState.Committed }
            }
        }
    }

    private fun cancel(next: MobileOAuthState = MobileOAuthState.Idle) {
        operation?.cancel()
        session = null
        updateState { next }
    }

    private fun run(block: suspend () -> MobileOAuthState) {
        operation = viewModelScope.launch {
            updateState { MobileOAuthState.Busy }
            val next = block()
            if (next !is MobileOAuthState.Google && next !is MobileOAuthState.Yandex) session = null
            updateState { next }
        }
    }
}

private inline fun <T> AuthResult<T>.map(success: (T) -> MobileOAuthState): MobileOAuthState = when (this) {
    is AuthResult.Success -> success(value)
    is AuthResult.Failed -> MobileOAuthState.Error(failure)
}
