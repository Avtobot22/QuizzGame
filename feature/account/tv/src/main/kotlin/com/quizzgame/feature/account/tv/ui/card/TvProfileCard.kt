package com.quizzgame.feature.account.tv.ui.card

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.QuizzProfileAvatar
import com.quizzgame.core.ui.QuizzProfilePanel
import com.quizzgame.feature.account.tv.R
import com.quizzgame.feature.account.tv.presentation.TvProfileState
import com.quizzgame.feature.account.tv.ui.tags.TvProfileTags
import com.quizzgame.shared.splash.domain.entity.AccountProfile

@Composable
internal fun TvProfileLoaded(
    state: TvProfileState.Loaded,
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    retry: () -> Unit,
) {
    when (val profile = state.profile) {
        AccountProfile.Guest -> GuestProfile(onSignIn)
        is AccountProfile.SignedIn -> {
            val displayName = profile.user?.displayName?.takeIf(String::isNotBlank)
            ProfileIdentity(
                title = displayName ?: stringResource(R.string.profile_signed_in),
                displayName = displayName,
            )
            if (profile.user == null) TvProfileRecovery(state.refreshing, retry)
            TvProfileAccountActions(onSettings, onLogout)
        }
        AccountProfile.Unavailable -> TvProfileRecovery(state.refreshing, retry, requestFocus = true)
    }
}

@Composable
private fun GuestProfile(onSignIn: () -> Unit) {
    val focus = remember { FocusRequester() }
    ProfileIdentity(stringResource(R.string.profile_guest_title), stringResource(R.string.profile_guest))
    QuizzTvButton(onSignIn, Modifier.focusRequester(focus).testTag(TvProfileTags.SIGN_IN)) {
        Text(stringResource(R.string.profile_sign_in))
    }
    TvEntryFocus(focus)
}

@Composable
private fun ProfileIdentity(title: String, subtitle: String? = null, displayName: String? = null) {
    val colors = MaterialTheme.colorScheme
    QuizzProfilePanel(
        colors.surfaceVariant.copy(alpha = 0.35f),
        colors.primaryContainer,
        MaterialTheme.shapes.extraLarge,
    ) {
        QuizzProfileAvatar(
            displayName,
            colors.primaryContainer,
            colors.onPrimaryContainer,
            MaterialTheme.typography.headlineMedium,
            Modifier.size(76.dp),
        )
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
