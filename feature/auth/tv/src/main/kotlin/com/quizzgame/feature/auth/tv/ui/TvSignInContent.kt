package com.quizzgame.feature.auth.tv.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvCenteredContent
import com.quizzgame.core.designsystem.tv.QuizzTvProgressStrip
import com.quizzgame.core.designsystem.tv.QuizzTvScreenScaffold
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.presentation.TvSignInIntent
import com.quizzgame.feature.auth.tv.presentation.TvSignInState
import com.quizzgame.feature.auth.tv.presentation.TvSignInStep
import com.quizzgame.feature.auth.tv.ui.code.TvCodeScreen
import com.quizzgame.feature.auth.tv.ui.intro.TvAuthIntro
import com.quizzgame.feature.auth.tv.ui.intro.TvAuthSuccess
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import kotlinx.coroutines.delay

@Composable
internal fun TvSignInContent(
    state: TvSignInState,
    onIntent: (TvSignInIntent) -> Unit,
    onGoogle: () -> Unit,
    onCancel: () -> Unit,
    recovery: Boolean,
    previewElapsedMillis: Long? = null,
) {
    val back = {
        when {
            state.step == TvSignInStep.Finished -> Unit
            state.step is TvSignInStep.Identifier || state.step is TvSignInStep.Code -> onIntent(TvSignInIntent.Back)
            else -> onCancel()
        }
    }
    if (!LocalInspectionMode.current) BackHandler { back() }
    val elapsed = previewElapsedMillis ?: rememberAuthElapsed(state)
    val step = state.step
    QuizzTvScreenScaffold(modifier = Modifier.testTag(TvSignInTags.SCREEN)) {
        when {
            step == TvSignInStep.Finished -> QuizzTvCenteredContent { TvAuthSuccess() }
            step is TvSignInStep.Code ->
                TvCodeScreen(step, state, elapsed, back, onIntent)
            else -> TvAuthStage(state, elapsed, recovery, onIntent, onGoogle, back)
        }
    }
}

@Composable
private fun TvAuthStage(
    state: TvSignInState,
    elapsed: Long,
    recovery: Boolean,
    onIntent: (TvSignInIntent) -> Unit,
    onGoogle: () -> Unit,
    back: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    QuizzTvCenteredContent {
        if (state.methodsUnavailable) {
            TvAuthStepContent(state, elapsed, back, onIntent, onGoogle)
            return@QuizzTvCenteredContent
        }
        TvAuthIntro(state, recovery)
        Column(
            Modifier.widthIn(max = 500.dp).fillMaxWidth()
                .background(colors.surfaceVariant.copy(alpha = 0.35f), MaterialTheme.shapes.extraLarge)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TvAuthStageProgress(state)
            TvAuthStepContent(state, elapsed, back, onIntent, onGoogle)
            if (state.step !is TvSignInStep.Identifier) TvAuthCancelAction(state, back)
        }
    }
}

@Composable
private fun TvAuthStageProgress(state: TvSignInState) {
    if (state.step is TvSignInStep.Identifier) {
        Box(Modifier.fillMaxWidth().height(4.dp)) {
            if (state.busy) QuizzTvProgressStrip(stringResource(R.string.auth_busy))
        }
    }
}

@Composable
private fun TvAuthCancelAction(state: TvSignInState, back: () -> Unit) {
    val focus = remember { FocusRequester() }
    QuizzTvButton(back, Modifier.focusRequester(focus), style = QuizzTvButtonStyle.QUIET) {
        Text(stringResource(R.string.auth_cancel))
    }
    val loadingMethods = state.step == TvSignInStep.Loading && state.failure == null
    TvEntryFocus(focus, loadingMethods)
}

@Composable
private fun rememberAuthElapsed(state: TvSignInState): Long {
    val step = state.step as? TvSignInStep.Code
    val cooldown = maxOf(state.requestBlockedUntil, step?.form?.resendAtMillis ?: 0)
    val until = maxOf(cooldown, step?.form?.expiresAtMillis ?: 0)
    val elapsed by produceState(SystemClock.elapsedRealtime(), cooldown, until) {
        value = SystemClock.elapsedRealtime()
        while (value < until) {
            val interval = if (value < cooldown) 250L else until - value
            delay(interval.coerceAtLeast(1))
            value = SystemClock.elapsedRealtime()
        }
    }
    return elapsed
}
