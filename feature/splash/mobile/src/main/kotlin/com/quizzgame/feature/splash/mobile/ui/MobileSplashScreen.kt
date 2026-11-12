package com.quizzgame.feature.splash.mobile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorFullScreen
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.core.ui.QuizzBrandLoading
import com.quizzgame.feature.splash.mobile.R
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashDestination
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashIntent
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashIntent.NavigationHandled
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashIntent.Retry
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashIntent.SignIn
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashUiState
import com.quizzgame.feature.splash.mobile.presentation.MobileSplashViewModel
import com.quizzgame.core.ui.R as UiR

@Composable
fun MobileSplashScreen(
    onJoinRoom: () -> Boolean,
    onSignIn: () -> Boolean,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MobileSplashViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    MobileSplashContent(
        state = state,
        onIntent = viewModel::onIntent,
        onJoinRoom = onJoinRoom,
        onSignIn = onSignIn,
        onExit = onExit,
        modifier = modifier,
    )
}

@Composable
private fun MobileSplashContent(
    state: MobileSplashUiState,
    onIntent: (MobileSplashIntent) -> Unit,
    onJoinRoom: () -> Boolean,
    onSignIn: () -> Boolean,
    onExit: () -> Unit,
    modifier: Modifier,
) {
    val result = state as? MobileSplashUiState.NavigationPending
    NavigationResultEffect(
        result,
        onHandled = { onIntent(NavigationHandled(it.requestId)) },
    ) {
        when (it.destination) {
            MobileSplashDestination.JOIN_ROOM -> onJoinRoom()
            MobileSplashDestination.SIGN_IN   -> onSignIn()
        }
    }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is MobileSplashUiState.Failed -> QuizzMobileErrorFullScreen(
                state.spec,
                onAction = { action ->
                    when (action) {
                        ErrorActionKind.RETRY   -> onIntent(Retry)
                        ErrorActionKind.SIGN_IN -> onIntent(SignIn)
                        ErrorActionKind.EXIT    -> onExit()
                        else                    -> Unit
                    }
                },
            )

            else                          ->
                QuizzBrandLoading(
                    label = stringResource(R.string.splash_accessibility_loading),
                    primary = MaterialTheme.colorScheme.primary,
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.testTag(MobileSplashTags.LOADING),
                    title = {
                        Text(
                            stringResource(UiR.string.quizz_brand_name),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                )
        }
    }
}
