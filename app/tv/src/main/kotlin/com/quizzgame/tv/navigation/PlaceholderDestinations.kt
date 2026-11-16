package com.quizzgame.tv.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.tv.QuizzTvMessageScreen
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.tv.R

@Composable
internal fun TvSignInDestination() {
    QuizzTvMessageScreen(
        stringResource(R.string.signin_title),
        stringResource(R.string.signin_placeholder),
        icon = QuizzStatusIcon.AUTH,
    )
}
