package com.quizzgame.feature.authprovider.mobile.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.quizzgame.feature.authprovider.mobile.data.GoogleCredentialAdapter
import com.quizzgame.feature.authprovider.mobile.presentation.MobileOAuthIntent
import com.quizzgame.feature.authprovider.mobile.presentation.MobileOAuthState
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun MobileOAuthEffects(
    state: MobileOAuthState,
    claimBrowserLaunch: (String) -> Boolean,
    onIntent: (MobileOAuthIntent) -> Unit,
) {
    val activity = LocalActivity.current ?: return
    when (state) {
        is MobileOAuthState.Google -> {
            val challenge = state.attempt.challenge
            LaunchedEffect(challenge.id, activity) {
                val result = GoogleCredentialAdapter.request(activity, challenge.nonce)
                onIntent(MobileOAuthIntent.GoogleReturned(challenge.id, result))
            }
        }
        is MobileOAuthState.Yandex -> {
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onIntent(MobileOAuthIntent.BrowserReturned) }
            val attempt = state.attempt.attempt
            LaunchedEffect(attempt.id, activity) {
                try {
                    if (claimBrowserLaunch(attempt.id)) {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(attempt.url.toASCIIString())))
                    }
                    delay(Duration.between(Instant.now(), attempt.expiresAt).toMillis().coerceAtLeast(0).milliseconds)
                    onIntent(MobileOAuthIntent.BrowserUnavailable)
                } catch (_: ActivityNotFoundException) {
                    onIntent(MobileOAuthIntent.BrowserUnavailable)
                }
            }
        }
        else -> Unit
    }
}
