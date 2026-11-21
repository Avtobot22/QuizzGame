package com.quizzgame.feature.auth.mobile.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInIntent
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInState
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInStep
import com.quizzgame.feature.auth.mobile.ui.intro.MobileAuthIntro
import com.quizzgame.feature.auth.mobile.ui.intro.MobileAuthSuccess
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun MobileSignInContent(
    state: MobileSignInState,
    onIntent: (MobileSignInIntent) -> Unit,
    onProvider: (AuthMethod) -> Unit,
    onCancel: () -> Unit,
    recovery: Boolean,
    previewElapsedMillis: Long? = null,
) {
    val isInput = state.step is MobileSignInStep.Identifier || state.step is MobileSignInStep.Code
    val complete = state.step == MobileSignInStep.Finished
    val showMethodsStatus = state.methodsUnavailable
    val back = {
        when {
            complete -> Unit
            isInput -> onIntent(MobileSignInIntent.Back)
            else -> onCancel()
        }
    }
    if (!LocalInspectionMode.current) BackHandler { back() }
    val elapsed = previewElapsedMillis ?: rememberAuthElapsed(state)
    QuizzScreenScaffold(
        modifier = Modifier.testTag(MobileSignInTags.SCREEN),
        topBar = {
            if (!complete && !showMethodsStatus) QuizzTopBar(stringResource(R.string.auth_account), onBack = back)
        },
    ) { padding ->
        val content = Modifier.padding(padding)
        when {
            complete -> Box(content.fillMaxSize(), contentAlignment = Alignment.Center) { MobileAuthSuccess() }
            showMethodsStatus -> MobileAuthMethodsStatus(
                state.failure,
                onBack = back,
                onRetry = { onIntent(MobileSignInIntent.Load) },
                modifier = content,
            )
            else -> MobileAuthStage(state, elapsed, onIntent, onProvider, recovery, content)
        }
    }
}

@Composable
private fun MobileAuthStage(
    state: MobileSignInState,
    elapsed: Long,
    onIntent: (MobileSignInIntent) -> Unit,
    onProvider: (AuthMethod) -> Unit,
    recovery: Boolean,
    modifier: Modifier,
) {
    val scroll = rememberScrollState()
    LaunchedEffect(state.step::class) { scroll.scrollTo(0) }
    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val compact = maxHeight < 400.dp
        Column(
            Modifier.widthIn(max = 456.dp).fillMaxWidth().verticalScroll(scroll)
                .padding(horizontal = QuizzMobileLayout.pageGutter, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MobileAuthIntro(state.step, recovery, compact)
            MobileAuthPanel(state, elapsed, onIntent, onProvider)
        }
    }
}

@Composable
private fun MobileAuthPanel(
    state: MobileSignInState,
    elapsed: Long,
    onIntent: (MobileSignInIntent) -> Unit,
    onProvider: (AuthMethod) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().height(4.dp)) {
                if (state.step !is MobileSignInStep.Loading && state.busy) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().height(4.dp))
                }
            }
            MobileAuthStepContent(state, elapsed, onIntent, onProvider)
        }
    }
}

@Composable
private fun rememberAuthElapsed(state: MobileSignInState): Long {
    val step = state.step as? MobileSignInStep.Code
    val cooldown = maxOf(state.requestBlockedUntil, step?.form?.resendAtMillis ?: 0)
    val until = maxOf(cooldown, step?.form?.expiresAtMillis ?: 0)
    val elapsed by produceState(SystemClock.elapsedRealtime(), cooldown, until) {
        value = SystemClock.elapsedRealtime()
        while (value < until) {
            val interval = if (value < cooldown) 250L else until - value
            delay(interval.coerceAtLeast(1).milliseconds)
            value = SystemClock.elapsedRealtime()
        }
    }
    return elapsed
}
