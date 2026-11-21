package com.quizzgame.feature.account.tv.ui.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionRow
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvButtonTone
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.designsystem.tv.QuizzTvProgressStrip
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.account.tv.R
import com.quizzgame.feature.account.tv.ui.tags.TvProfileTags
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvProfileAccountActions(onSettings: () -> Unit, onLogout: () -> Unit) {
    val focus = remember { FocusRequester() }
    QuizzTvActionRow {
        QuizzTvButton(onSettings, Modifier.focusRequester(focus).testTag(TvProfileTags.SETTINGS)) {
            ProfileActionLabel(UiR.drawable.ic_quizz_settings, R.string.profile_settings)
        }
        QuizzTvButton(
            onLogout,
            Modifier.testTag(TvProfileTags.SIGN_OUT),
            style = QuizzTvButtonStyle.QUIET,
            tone = QuizzTvButtonTone.DANGER,
        ) {
            ProfileActionLabel(UiR.drawable.ic_quizz_logout, R.string.profile_sign_out)
        }
    }
    TvEntryFocus(focus)
}

@Composable
internal fun TvProfileRecovery(refreshing: Boolean, retry: () -> Unit, requestFocus: Boolean = false) {
    val focus = remember { FocusRequester() }
    QuizzTvNoticeBanner(
        tone = QuizzNoticeTone.ERROR,
        icon = QuizzStatusIcon.AUTH,
        actions = {
            if (refreshing) {
                QuizzTvProgressStrip(stringResource(R.string.profile_loading))
            } else {
                QuizzTvButton(
                    retry,
                    Modifier.focusRequester(focus),
                    style = QuizzTvButtonStyle.SECONDARY,
                ) { Text(stringResource(R.string.profile_retry)) }
            }
        },
    ) {
        Text(stringResource(R.string.profile_unavailable), style = MaterialTheme.typography.titleSmall)
    }
    TvEntryFocus(focus, requestFocus && !refreshing)
}

@Composable
private fun ProfileActionLabel(icon: Int, label: Int) {
    QuizzIconLabel(icon, iconSize = QuizzIdentityDimensions.actionIcon.dp, tint = LocalContentColor.current) {
        Text(stringResource(label))
    }
}
