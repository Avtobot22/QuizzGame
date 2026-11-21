package com.quizzgame.tv.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.platform.app.InstrumentationRegistry
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.debug.navigation.FixtureSignInViewModel
import com.quizzgame.tv.ui.tags.TvFixtureTags
import com.quizzgame.tv.test.R as FixtureR

@Composable
internal fun TvFixtureSignInDestination(
    onContinue: () -> Boolean,
    viewModel: FixtureSignInViewModel = viewModel(),
) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    NavigationResultEffect(result, viewModel::resultHandled) { onContinue() }
    FixturePlaceholder(
        FixtureR.string.fixture_signin_title,
        listOf(FixtureR.string.fixture_restart to viewModel::requestRestart),
    )
}

@Composable
internal fun FixturePlaceholder(title: Int, actions: List<Pair<Int, () -> Unit>>) {
    val firstFocus = remember { FocusRequester() }
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(fixtureString(FixtureR.string.fixture_label))
        Text(fixtureString(title))
        actions.forEachIndexed { index, (label, action) ->
            QuizzTvButton(
                onClick = action,
                modifier = Modifier.testTag(TvFixtureTags.action(index))
                    .then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier),
            ) { Text(fixtureString(label)) }
        }
    }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
}

internal fun fixtureString(@StringRes id: Int, vararg args: Any): String =
    InstrumentationRegistry.getInstrumentation().context.getString(id, *args)
