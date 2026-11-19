package com.quizzgame.feature.authprovider.mobile.presentation

import com.quizzgame.feature.authprovider.mobile.data.ProviderCredential
import com.quizzgame.shared.auth.domain.entity.AuthMethod

internal sealed interface MobileOAuthIntent {
    data class Start(val method: AuthMethod) : MobileOAuthIntent
    data object Cancel : MobileOAuthIntent
    data class GoogleReturned(val challengeId: String, val result: ProviderCredential) : MobileOAuthIntent
    data class YandexReturned(val attemptId: String) : MobileOAuthIntent
    data object BrowserReturned : MobileOAuthIntent
    data object BrowserUnavailable : MobileOAuthIntent
}
