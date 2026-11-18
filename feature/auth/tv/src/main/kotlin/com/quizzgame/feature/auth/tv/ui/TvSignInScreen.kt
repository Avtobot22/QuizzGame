package com.quizzgame.feature.auth.tv.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvScreenScaffold
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.presentation.TvSignInViewModel
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import com.quizzgame.shared.auth.domain.entity.OtpIntent
import com.quizzgame.shared.auth.domain.entity.OtpState
import com.quizzgame.shared.auth.domain.entity.OtpStep
import kotlinx.coroutines.delay

@Composable
internal fun TvSignInScreen(onCancel: () -> Unit, recovery: Boolean = false) {
    val model: TvSignInViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    TvSignInContent(state, model::onIntent, onCancel, recovery)
}

@Composable
internal fun TvSignInContent(
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
    QuizzTvScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 720.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(QuizzTvLayout.screenInset.dp)
                    .testTag(TvSignInTags.SCREEN),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(if (state.step is OtpStep.Code) R.string.auth_enter_code else R.string.auth_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (recovery) Text(stringResource(R.string.auth_reauth))
                if (state.busy) Text(stringResource(R.string.auth_busy))
                TvAuthError(state.failure)
                TvAuthStepContent(state, elapsed, onIntent)
                QuizzTvButton(back, style = QuizzTvButtonStyle.QUIET) {
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
}
