package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzRoomCodeField
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.debug.navigation.FixtureSignInViewModel
import com.quizzgame.mobile.R
import com.quizzgame.mobile.ui.tags.MobileFixtureTags
import com.quizzgame.mobile.test.R as FixtureR

@Composable
internal fun MobileFixtureSignInDestination(
    onContinue: () -> Boolean,
    viewModel: FixtureSignInViewModel = viewModel(),
) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    NavigationResultEffect(result, viewModel::resultHandled) { onContinue() }
    FixturePlaceholder(
        R.string.signin_placeholder,
        listOf(FixtureR.string.fixture_restart to viewModel::requestRestart),
    )
}

@Composable
internal fun MobileFixturePlayDestination(
    onLogout: () -> Unit,
) {
    var roomCode by rememberSaveable { mutableStateOf("") }
    FixturePlaceholder(R.string.play_placeholder, listOf(FixtureR.string.fixture_signout to onLogout)) {
        QuizzRoomCodeField(
            value = roomCode,
            onValueChange = { roomCode = it },
            label = stringResource(FixtureR.string.fixture_room_code),
            modifier = Modifier.testTag(MobileFixtureTags.ROOM_CODE),
        )
    }
}

@Composable
internal fun FixturePlaceholder(
    title: Int,
    actions: List<Pair<Int, () -> Unit>>,
    content: @Composable () -> Unit = {},
) {
    QuizzScreenScaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(stringResource(FixtureR.string.fixture_label))
            Text(stringResource(title))
            content()
            actions.forEachIndexed { index, (label, action) ->
                QuizzButton(
                    onClick = action,
                    modifier = Modifier.testTag(MobileFixtureTags.action(index)),
                ) { Text(stringResource(label)) }
            }
        }
    }
}
