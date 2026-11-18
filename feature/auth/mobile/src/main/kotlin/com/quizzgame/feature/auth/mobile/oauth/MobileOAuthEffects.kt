package com.quizzgame.feature.auth.mobile.oauth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

val LocalMockOAuth = staticCompositionLocalOf { false }

@Composable
internal fun MobileOAuthEffects(
    state: MobileOAuthState,
    claimBrowserLaunch: (String) -> Boolean,
    onIntent: (MobileOAuthIntent) -> Unit,
) {
    val activity = LocalActivity.current ?: return
    val mock = LocalMockOAuth.current
    when (state) {
        is MobileOAuthState.Google -> LaunchedEffect(state.challenge.id, activity) {
            val result = if (mock) {
                ProviderCredential.Token("mock-google-id-token")
            } else {
                GoogleCredentialAdapter.request(activity, state.challenge.nonce)
            }
            onIntent(MobileOAuthIntent.GoogleReturned(state.challenge.id, result))
        }
        is MobileOAuthState.Yandex -> {
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onIntent(MobileOAuthIntent.BrowserReturned) }
            LaunchedEffect(state.attempt.id, activity) {
                val attempt = state.attempt
                if (mock) {
                    onIntent(MobileOAuthIntent.YandexReturned(attempt.id))
                } else {
                    try {
                        if (claimBrowserLaunch(attempt.id)) {
                            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(attempt.url.toASCIIString())))
                        }
                        delay(Duration.between(Instant.now(), attempt.expiresAt).toMillis().coerceAtLeast(0))
                        onIntent(MobileOAuthIntent.BrowserUnavailable)
                    } catch (_: ActivityNotFoundException) {
                        onIntent(MobileOAuthIntent.BrowserUnavailable)
                    }
                }
            }
        }
        else -> Unit
    }
}
