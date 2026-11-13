package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.mobile.R

@Composable
@Suppress("UnusedParameter")
internal fun MobileSignInDestination(onContinue: () -> Boolean) {
    QuizzScreenScaffold { padding -> Text(stringResource(R.string.signin_placeholder), Modifier.padding(padding)) }
}

@Composable
@Suppress("UnusedParameter")
internal fun MobileJoinRoomDestination(roomCode: String, onRoomCodeChange: (String) -> Unit, onLogout: () -> Unit) {
    QuizzScreenScaffold { padding -> Text(stringResource(R.string.join_room_placeholder), Modifier.padding(padding)) }
}
