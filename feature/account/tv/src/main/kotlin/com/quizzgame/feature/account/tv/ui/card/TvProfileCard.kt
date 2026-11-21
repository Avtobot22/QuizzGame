package com.quizzgame.feature.account.tv.ui.card

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.QuizzProfileAvatar
import com.quizzgame.core.ui.QuizzProfileIdentity
import com.quizzgame.core.ui.QuizzProfilePanel
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.account.tv.R
import com.quizzgame.feature.account.tv.ui.editor.TvNicknameEditor
import com.quizzgame.feature.account.tv.ui.tags.TvProfileTags
import com.quizzgame.shared.account.domain.entity.PlayerNickname
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.presentation.ProfileIntent
import com.quizzgame.shared.account.presentation.ProfileState
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvProfileLoaded(
    state: ProfileState.Ready,
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    onIntent: (ProfileIntent) -> Unit,
) {
    when (val profile = state.profile) {
        PlayerProfile.Guest -> GuestProfile(onSignIn)
        is PlayerProfile.SignedIn -> {
            val user = profile.user
            if (user == null) {
                ProfileIdentity(title = stringResource(R.string.profile_signed_in))
                TvProfileRecovery(state.refreshing, { onIntent(ProfileIntent.Retry) })
            } else {
                val displayName = user.displayName?.let(PlayerNickname::normalize)
                val editFocus = remember { FocusRequester() }
                ProfileIdentity(
                    title = displayName ?: stringResource(R.string.profile_signed_in),
                    subtitle = stringResource(R.string.profile_nickname_description),
                    displayName = displayName,
                ) {
                    QuizzTvButton(
                        { onIntent(ProfileIntent.EditNickname) },
                        Modifier.focusRequester(editFocus).testTag(TvProfileTags.EDIT_NICKNAME),
                        style = QuizzTvButtonStyle.SECONDARY,
                        enabled = !state.refreshing,
                    ) {
                        val label = if (displayName == null) {
                            R.string.profile_nickname_add
                        } else {
                            R.string.profile_nickname_edit
                        }
                        QuizzIconLabel(UiR.drawable.ic_quizz_edit, tint = LocalContentColor.current) {
                            Text(stringResource(label))
                        }
                    }
                }
                state.editor?.let { TvNicknameEditor(it, editFocus, onIntent) }
            }
            TvProfileAccountActions(onSettings, onLogout)
        }
        PlayerProfile.Unavailable -> TvProfileRecovery(
            state.refreshing,
            { onIntent(ProfileIntent.Retry) },
            requestFocus = true,
        )
    }
}

@Composable
private fun GuestProfile(onSignIn: () -> Unit) {
    val focus = remember { FocusRequester() }
    ProfileIdentity(stringResource(R.string.profile_guest_title), stringResource(R.string.profile_guest)) {
        QuizzTvButton(onSignIn, Modifier.focusRequester(focus).testTag(TvProfileTags.SIGN_IN)) {
            Text(stringResource(R.string.profile_sign_in))
        }
    }
    TvEntryFocus(focus)
}

@Composable
private fun ProfileIdentity(
    title: String,
    subtitle: String? = null,
    displayName: String? = null,
    action: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    QuizzProfilePanel(
        colors.surfaceVariant.copy(alpha = 0.35f),
        colors.primaryContainer,
        MaterialTheme.shapes.extraLarge,
    ) {
        QuizzProfileIdentity(avatar = {
            QuizzProfileAvatar(
                displayName,
                colors.primaryContainer,
                colors.onPrimaryContainer,
                MaterialTheme.typography.headlineMedium,
                Modifier.size(QuizzIdentityDimensions.tvAvatar.dp),
            )
        }) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        action()
    }
}
