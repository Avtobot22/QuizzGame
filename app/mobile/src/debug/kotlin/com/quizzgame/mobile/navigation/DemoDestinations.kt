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
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzRoomCodeField
import com.quizzgame.mobile.R

@Composable
internal fun MobileSplashDestination(onSignIn: () -> Unit, onContinue: () -> Unit) {
    DemoPlaceholder(
        R.string.splash_placeholder,
        listOf(
            R.string.demo_signin to onSignIn,
            R.string.demo_continue to onContinue,
        ),
    )
}

@Composable
internal fun MobileSignInDestination(onContinue: () -> Unit) {
    DemoPlaceholder(R.string.signin_placeholder, listOf(R.string.demo_continue to onContinue))
}

@Composable
internal fun MobileJoinRoomDestination(onSignIn: () -> Unit) {
    var roomCode by rememberSaveable { mutableStateOf("") }
    DemoPlaceholder(R.string.join_room_placeholder, listOf(R.string.demo_signout to onSignIn)) {
        QuizzRoomCodeField(
            value = roomCode,
            onValueChange = { roomCode = it },
            label = stringResource(R.string.demo_room_code),
            modifier = Modifier.testTag("demo-room-code"),
        )
    }
}

@Composable
internal fun DemoPlaceholder(
    title: Int,
    actions: List<Pair<Int, () -> Unit>>,
    content: @Composable () -> Unit = {},
) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.demo_label))
        Text(stringResource(title))
        content()
        actions.forEachIndexed { index, (label, action) ->
            QuizzButton(
                onClick = action,
                modifier = Modifier.testTag("demo-action-$index"),
            ) { Text(stringResource(label)) }
        }
    }
}
