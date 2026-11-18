package com.quizzgame.feature.auth.mobile.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.oauth.GoogleCredentialAdapter
import com.quizzgame.feature.auth.mobile.oauth.LocalMockOAuth
import com.quizzgame.feature.auth.mobile.oauth.MobileOAuthEffects
import com.quizzgame.feature.auth.mobile.oauth.MobileOAuthIntent
import com.quizzgame.feature.auth.mobile.oauth.MobileOAuthState
import com.quizzgame.feature.auth.mobile.oauth.MobileOAuthViewModel
import com.quizzgame.feature.auth.mobile.oauth.YandexAppLink
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInViewModel
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.OtpIntent
import com.quizzgame.shared.auth.domain.entity.OtpState
import com.quizzgame.shared.auth.domain.entity.OtpStep
import kotlinx.coroutines.delay

@Composable
internal fun MobileSignInScreen(onCancel: () -> Unit, recovery: Boolean = false) {
    val model: MobileSignInViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    val oauth: MobileOAuthViewModel = hiltViewModel()
    val providerState by oauth.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val mock = LocalMockOAuth.current
    val methods = setOfNotNull(
        AuthMethod.GOOGLE.takeIf { mock || GoogleCredentialAdapter.available(context) },
        AuthMethod.YANDEX.takeIf { mock || YandexAppLink.available(context) },
    )
    val visible = state.withProviders(methods)
    val providerActive = providerState == MobileOAuthState.Busy || providerState is MobileOAuthState.Google ||
        providerState is MobileOAuthState.Yandex || providerState == MobileOAuthState.Committed
    MobileOAuthEffects(providerState, oauth::claimBrowserLaunch, oauth::onIntent)
    MobileSignInContent(
        visible.copy(
            busy = state.busy || providerActive,
            failure = (providerState as? MobileOAuthState.Error)?.failure ?: state.failure,
        ),
        { intent ->
            if (intent is OtpIntent.Choose && intent.method in methods) {
                oauth.onIntent(MobileOAuthIntent.Start(intent.method))
            } else {
                if (!providerActive) {
                    oauth.onIntent(MobileOAuthIntent.Cancel)
                    model.onIntent(intent)
                }
            }
        },
        onCancel,
        recovery,
    )
}

@Composable
internal fun MobileSignInContent(
    state: OtpState,
    onIntent: (OtpIntent) -> Unit,
    onCancel: () -> Unit,
    recovery: Boolean,
) {
    val isInput = state.step is OtpStep.Identifier || state.step is OtpStep.Code
    val back = { if (isInput) onIntent(OtpIntent.Back) else onCancel() }
    if (!LocalInspectionMode.current) BackHandler { back() }
    val elapsed by produceState(SystemClock.elapsedRealtime(), state.step, state.requestBlockedUntil) {
        value = SystemClock.elapsedRealtime()
        val step = state.step as? OtpStep.Code
        val cooldown = maxOf(state.requestBlockedUntil, step?.resendAtMillis ?: 0)
        val until = maxOf(cooldown, step?.expiresAtMillis ?: 0)
        while (value < until) {
            val interval = if (value < cooldown) 250L else until - value
            delay(interval.coerceAtLeast(1))
            value = SystemClock.elapsedRealtime()
        }
    }
    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = QuizzMobileLayout.dialogWidth).fillMaxWidth()
                .verticalScroll(rememberScrollState()).padding(QuizzMobileLayout.pageGutter)
                .testTag(MobileSignInTags.SCREEN),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(if (state.step is OtpStep.Code) R.string.auth_enter_code else R.string.auth_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            if (recovery) Text(stringResource(R.string.auth_reauth))
            if (state.busy) Text(stringResource(R.string.auth_busy))
            MobileAuthError(state.failure)
            MobileAuthStepContent(state, elapsed, onIntent)
            QuizzButton(back, style = QuizzButtonStyle.TEXT) {
                val label = when (state.step) {
                    is OtpStep.Code -> R.string.auth_change_destination
                    is OtpStep.Identifier -> R.string.auth_back
                    else -> R.string.auth_cancel
                }
                Text(stringResource(label))
            }
        }
    }
}

private fun OtpState.withProviders(providers: Set<AuthMethod>): OtpState {
    val methods = step as? OtpStep.Methods ?: return this
    return copy(
        step = methods.copy(
            choices = methods.choices.filter {
                it == AuthMethod.EMAIL || it == AuthMethod.SMS || it in providers
            },
        ),
    )
}
