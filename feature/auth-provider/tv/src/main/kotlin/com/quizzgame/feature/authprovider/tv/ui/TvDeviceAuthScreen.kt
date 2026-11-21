package com.quizzgame.feature.authprovider.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvCenteredContent
import com.quizzgame.core.designsystem.tv.QuizzTvScreenScaffold
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.QuizzAuthArtwork
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.authprovider.tv.R
import com.quizzgame.feature.authprovider.tv.presentation.TvDeviceAuthIntent
import com.quizzgame.feature.authprovider.tv.presentation.TvDeviceAuthState
import com.quizzgame.feature.authprovider.tv.presentation.TvDeviceAuthViewModel
import com.quizzgame.core.ui.R as UiR

@Composable
fun TvDeviceAuthScreen(signIn: @Composable (startDeviceAuth: (() -> Unit)?) -> Unit) {
    val viewModel: TvDeviceAuthViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val start = { viewModel.onIntent(TvDeviceAuthIntent.Start) }
    val back = { viewModel.onIntent(TvDeviceAuthIntent.Back) }

    if (state == TvDeviceAuthState.Idle) {
        signIn(start.takeIf { viewModel.available })
    } else {
        LifecycleResumeEffect(viewModel) {
            viewModel.onIntent(TvDeviceAuthIntent.Foreground(true))
            onPauseOrDispose { viewModel.onIntent(TvDeviceAuthIntent.Foreground(false)) }
        }
        BackHandler(onBack = back)
        TvDeviceAuthStage(state, onRetry = start, onBack = back)
    }
}

@Composable
internal fun TvDeviceAuthStage(state: TvDeviceAuthState, onRetry: () -> Unit, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    QuizzTvScreenScaffold {
        QuizzTvCenteredContent {
            TvDeviceAuthIntro()
            Column(
                Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .background(colors.surfaceVariant.copy(alpha = 0.35f), MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TvDeviceAuthContent(state, onRetry, onBack)
                if (state == TvDeviceAuthState.Starting || state is TvDeviceAuthState.Waiting) {
                    val focus = remember { FocusRequester() }
                    QuizzTvButton(onBack, Modifier.focusRequester(focus), style = QuizzTvButtonStyle.QUIET) {
                        Text(stringResource(R.string.auth_device_back))
                    }
                    TvEntryFocus(focus)
                }
            }
        }
    }
}

@Composable
private fun TvDeviceAuthIntro() {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        QuizzAuthArtwork(
            UiR.drawable.ic_quizz_google_g,
            colors.primary,
            colors.tertiary,
            QuizzIdentityDimensions.tvArtwork.dp,
            QuizzIdentityDimensions.tvArtworkIcon.dp,
            branded = true,
        )
        Text(
            stringResource(R.string.auth_device_google),
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.auth_device_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
