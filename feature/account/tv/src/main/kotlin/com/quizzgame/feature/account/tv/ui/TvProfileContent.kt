package com.quizzgame.feature.account.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvCenteredContent
import com.quizzgame.feature.account.tv.R
import com.quizzgame.feature.account.tv.ui.card.TvProfileLoaded
import com.quizzgame.feature.account.tv.ui.card.TvProfileLoading
import com.quizzgame.feature.account.tv.ui.tags.TvProfileTags
import com.quizzgame.shared.account.presentation.ProfileIntent
import com.quizzgame.shared.account.presentation.ProfileState

@Composable
internal fun TvProfileContent(
    state: ProfileState,
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    onIntent: (ProfileIntent) -> Unit,
) {
    QuizzTvCenteredContent(
        Modifier.testTag(TvProfileTags.SCREEN),
        maxWidth = 640.dp,
        spacing = 18.dp,
    ) {
        Text(
            stringResource(R.string.profile_title),
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        when (state) {
            ProfileState.Loading -> TvProfileLoading()
            is ProfileState.Ready -> TvProfileLoaded(state, onSignIn, onLogout, onSettings, onIntent)
        }
    }
}
