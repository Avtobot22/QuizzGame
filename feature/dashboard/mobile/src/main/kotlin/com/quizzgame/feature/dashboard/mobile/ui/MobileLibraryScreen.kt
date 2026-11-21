package com.quizzgame.feature.dashboard.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzMessageScreen
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.dashboard.mobile.R

@Composable
internal fun MobileLibraryScreen() {
    QuizzMessageScreen(
        title = stringResource(R.string.library_title),
        message = stringResource(R.string.library_unavailable),
        icon = QuizzStatusIcon.CATALOG,
    )
}
