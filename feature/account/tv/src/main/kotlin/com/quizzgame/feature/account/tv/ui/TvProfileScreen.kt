package com.quizzgame.feature.account.tv.ui


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvCatalogSkeleton
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.feature.account.tv.R
import com.quizzgame.feature.account.tv.presentation.TvProfileIntent
import com.quizzgame.feature.account.tv.presentation.TvProfileState
import com.quizzgame.feature.account.tv.presentation.TvProfileViewModel
import com.quizzgame.feature.account.tv.ui.tags.TvProfileTags
import com.quizzgame.shared.splash.domain.entity.AccountProfile

@Composable
fun TvProfileScreen(onSignIn: () -> Unit, onLogout: () -> Unit, onSettings: () -> Unit) {
    val model: TvProfileViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    val title = stringResource(R.string.profile_title)
    Column(
        Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 28.dp).testTag(TvProfileTags.SCREEN),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        when (val current = state) {
            TvProfileState.Loading -> QuizzTvCatalogSkeleton(title)
            is TvProfileState.Loaded -> ProfileActions(
                current.profile,
                onSignIn,
                onLogout,
                onSettings,
                { model.onIntent(TvProfileIntent.RETRY) },
            )
        }
    }
}

@Composable
private fun ProfileActions(
    profile: AccountProfile,
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    retry: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    val first = Modifier.focusRequester(focus)
    when (profile) {
        AccountProfile.Guest -> {
            Text(stringResource(R.string.profile_guest))
            QuizzTvButton(onSignIn, first.testTag(TvProfileTags.SIGN_IN)) {
                Text(stringResource(R.string.profile_sign_in))
            }
        }
        is AccountProfile.SignedIn -> {
            Text(profile.user?.displayName ?: stringResource(R.string.profile_signed_in))
            if (profile.user == null) {
                Text(stringResource(R.string.profile_unavailable))
                QuizzTvButton(retry, first) { Text(stringResource(R.string.profile_retry)) }
            }
            QuizzTvButton(onSettings, if (profile.user != null) first else Modifier) {
                Text(stringResource(R.string.profile_settings))
            }
            QuizzTvButton(onLogout, Modifier.testTag(TvProfileTags.SIGN_OUT)) {
                Text(stringResource(R.string.profile_sign_out))
            }
        }
        AccountProfile.Unavailable -> {
            Text(stringResource(R.string.profile_unavailable))
            QuizzTvButton(retry, first) { Text(stringResource(R.string.profile_retry)) }
        }
    }
    TvEntryFocus(focus, true)
}
