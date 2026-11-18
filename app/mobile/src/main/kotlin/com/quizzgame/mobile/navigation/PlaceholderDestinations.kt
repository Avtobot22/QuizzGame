package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzMessageScreen
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.mobile.R

@Composable
internal fun MobileSignInDestination() {
    QuizzScreenScaffold(topBar = { QuizzTopBar(stringResource(R.string.signin_title)) }) { padding ->
        QuizzMessageScreen(
            stringResource(R.string.signin_title),
            stringResource(R.string.signin_placeholder),
            Modifier.padding(padding),
            icon = QuizzStatusIcon.AUTH,
        )
    }
}
