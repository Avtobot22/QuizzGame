package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.quizzgame.debug.navigation.DemoSignInViewModel
import com.quizzgame.mobile.R

@Composable
internal fun MobileSignInDestination(
    onContinue: () -> Boolean,
    viewModel: DemoSignInViewModel = viewModel(),
) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    NavigationResultEffect(result, viewModel::resultHandled) { onContinue() }
    DemoPlaceholder(R.string.signin_placeholder, listOf(R.string.demo_restart to viewModel::requestRestart))
}

@Composable
internal fun MobileJoinRoomDestination(roomCode: String, onRoomCodeChange: (String) -> Unit, onLogout: () -> Unit) {
    DemoPlaceholder(R.string.join_room_placeholder, listOf(R.string.demo_signout to onLogout)) {
        QuizzRoomCodeField(
            value = roomCode,
            onValueChange = onRoomCodeChange,
            label = stringResource(R.string.demo_room_code),
            modifier = Modifier.testTag(MobileDemoTags.ROOM_CODE),
        )
    }
}

@Composable
internal fun DemoPlaceholder(
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
            Text(stringResource(R.string.demo_label))
            Text(stringResource(title))
            content()
            actions.forEachIndexed { index, (label, action) ->
                QuizzButton(
                    onClick = action,
                    modifier = Modifier.testTag(MobileDemoTags.action(index)),
                ) { Text(stringResource(label)) }
            }
        }
    }
}
