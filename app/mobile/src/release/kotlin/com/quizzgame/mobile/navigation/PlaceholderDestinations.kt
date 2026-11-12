package com.quizzgame.mobile.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quizzgame.mobile.R

@Composable
@Suppress("UnusedParameter")
internal fun MobileSignInDestination(onContinue: () -> Unit) {
    Text(stringResource(R.string.signin_placeholder))
}

@Composable
@Suppress("UnusedParameter")
internal fun MobileJoinRoomDestination(roomCode: String, onRoomCodeChange: (String) -> Unit, onLogout: () -> Unit) {
    Text(stringResource(R.string.join_room_placeholder))
}
