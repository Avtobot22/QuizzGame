package com.quizzgame.tv.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.core.designsystem.tv.QuizzTvConfirmationDialog
import com.quizzgame.feature.account.tv.ui.TvProfileScreen
import com.quizzgame.tv.R

@Composable
internal fun TvRoomProfileScreen(onSignIn: () -> Unit, onLogout: () -> Unit, onSettings: () -> Unit) {
    val model: TvRoomProfileViewModel = hiltViewModel()
    val room by model.session.collectAsStateWithLifecycle()
    var confirm by rememberSaveable { mutableStateOf(false) }
    TvProfileScreen(onSignIn, { if (room?.accountBound == true) confirm = true else onLogout() }, onSettings)
    if (confirm) {
        QuizzTvConfirmationDialog(
            stringResource(R.string.room_logout_title),
            stringResource(R.string.room_logout_description),
            {
                confirm = false
                onLogout()
            },
            { confirm = false },
            confirmLabel = stringResource(R.string.room_logout_confirm),
        )
    }
}
