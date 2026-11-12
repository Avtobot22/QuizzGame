package com.quizzgame.feature.splash.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorFullScreen
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.core.ui.QuizzBrandLoading
import com.quizzgame.core.ui.QuizzBrandLoadingSize
import com.quizzgame.feature.splash.tv.R
import com.quizzgame.feature.splash.tv.navigation.TvSplashDestination
import com.quizzgame.feature.splash.tv.presentation.TvSplashIntent
import com.quizzgame.feature.splash.tv.presentation.TvSplashIntent.NavigationHandled
import com.quizzgame.feature.splash.tv.presentation.TvSplashIntent.Retry
import com.quizzgame.feature.splash.tv.presentation.TvSplashIntent.SignIn
import com.quizzgame.feature.splash.tv.presentation.TvSplashUiState
import com.quizzgame.feature.splash.tv.presentation.TvSplashViewModel
import com.quizzgame.core.ui.R as UiR

@Composable
fun TvSplashScreen(
    onDashboard: () -> Boolean,
    onSignIn: () -> Boolean,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: TvSplashViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TvSplashContent(
        state = state,
        onIntent = viewModel::onIntent,
        onDashboard = onDashboard,
        onSignIn = onSignIn,
        onExit = onExit,
        modifier = modifier,
    )
}

@Composable
private fun TvSplashContent(
    state: TvSplashUiState,
    onIntent: (TvSplashIntent) -> Unit,
    onDashboard: () -> Boolean,
    onSignIn: () -> Boolean,
    onExit: () -> Unit,
    modifier: Modifier,
) {
    val result = state as? TvSplashUiState.NavigationPending
    NavigationResultEffect(
        result,
        onHandled = { onIntent(NavigationHandled) },
    ) {
        when (it.destination) {
            TvSplashDestination.DASHBOARD -> onDashboard()
            TvSplashDestination.SIGN_IN   -> onSignIn()
        }
    }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            is TvSplashUiState.Failed -> QuizzTvErrorFullScreen(
                current.spec,
                onAction = { action ->
                    when (action) {
                        ErrorActionKind.RETRY   -> onIntent(Retry)
                        ErrorActionKind.SIGN_IN -> onIntent(SignIn)
                        ErrorActionKind.EXIT    -> onExit()
                        else                    -> Unit
                    }
                },
            )
            else                      -> QuizzBrandLoading(
                label = stringResource(R.string.splash_accessibility_loading),
                primary = MaterialTheme.colorScheme.primary,
                accent = MaterialTheme.colorScheme.tertiary,
                title = {
                    Text(
                        stringResource(UiR.string.quizz_brand_name),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                modifier = Modifier.testTag(TvSplashTags.LOADING),
                size = QuizzBrandLoadingSize.EXPANDED,
            )
        }
    }
}
