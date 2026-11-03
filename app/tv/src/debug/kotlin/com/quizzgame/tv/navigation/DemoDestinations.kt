package com.quizzgame.tv.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.tv.R
import com.quizzgame.tv.dashboard.demo.DemoDashboard

@Composable
internal fun TvSplashDestination(onSignIn: () -> Unit, onContinue: () -> Unit) {
    DemoPlaceholder(
        R.string.splash_placeholder,
        listOf(
            R.string.demo_signin to onSignIn,
            R.string.demo_continue to onContinue,
        ),
    )
}

@Composable
internal fun TvSignInDestination(onContinue: () -> Unit) {
    DemoPlaceholder(R.string.signin_placeholder, listOf(R.string.demo_continue to onContinue))
}

@Composable
internal fun TvDashboardDestination(onSignIn: () -> Unit) {
    DemoDashboard(onSignIn)
}

@Composable
internal fun DemoPlaceholder(title: Int, actions: List<Pair<Int, () -> Unit>>) {
    val firstFocus = remember { FocusRequester() }
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.demo_label))
        Text(stringResource(title))
        actions.forEachIndexed { index, (label, action) ->
            QuizzTvButton(
                onClick = action,
                modifier = Modifier.testTag("demo-action-$index")
                    .then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier),
            ) { Text(stringResource(label)) }
        }
    }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
}
