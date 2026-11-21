package com.quizzgame.feature.account.mobile.ui.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.designsystem.mobile.QuizzMobileSkeleton
import com.quizzgame.core.designsystem.mobile.QuizzNoticeAction
import com.quizzgame.core.designsystem.mobile.QuizzNoticeBanner
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.QuizzProfileAvatar
import com.quizzgame.core.ui.QuizzProfileIdentity
import com.quizzgame.core.ui.QuizzProfilePanel
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzIdentityDimensions
import com.quizzgame.feature.account.mobile.R
import com.quizzgame.feature.account.mobile.ui.tags.MobileProfileTags
import com.quizzgame.shared.account.domain.entity.PlayerNickname
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun ProfileCard(
    profile: PlayerProfile,
    refreshing: Boolean,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    onEditNickname: () -> Unit,
) {
    val user = (profile as? PlayerProfile.SignedIn)?.user
    val guest = profile == PlayerProfile.Guest
    if (!guest && user == null) {
        ProfileRecovery(refreshing, onRetry)
        return
    }
    val nickname = user?.displayName?.let(PlayerNickname::normalize)
    val title = if (guest) {
        stringResource(R.string.mobile_profile_guest)
    } else {
        nickname ?: stringResource(R.string.mobile_profile_member)
    }
    val description = if (guest) {
        stringResource(R.string.mobile_profile_guest_description)
    } else {
        user?.email ?: user?.phoneNumber ?: stringResource(R.string.mobile_profile_member_description)
    }
    val colors = MaterialTheme.colorScheme
    QuizzProfilePanel(colors.surfaceContainer, colors.primaryContainer, MaterialTheme.shapes.extraLarge) {
        QuizzProfileIdentity(avatar = {
            QuizzProfileAvatar(
                nickname,
                colors.primaryContainer,
                colors.onPrimaryContainer,
                MaterialTheme.typography.headlineSmall,
                Modifier.size(QuizzIdentityDimensions.mobileAvatar.dp),
            )
        }) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            if (!guest) {
                Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        if (guest) {
            Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            QuizzButton(
                onSignIn,
                Modifier.fillMaxWidth().testTag(MobileProfileTags.SIGN_IN),
            ) { Text(stringResource(R.string.mobile_profile_sign_in)) }
        } else {
            QuizzButton(
                onEditNickname,
                Modifier.fillMaxWidth().testTag(MobileProfileTags.EDIT_NICKNAME),
                style = if (nickname == null) QuizzButtonStyle.FILLED else QuizzButtonStyle.OUTLINED,
                enabled = !refreshing,
            ) {
                val label = if (nickname == null) R.string.profile_nickname_add else R.string.profile_nickname_edit
                QuizzIconLabel(UiR.drawable.ic_quizz_edit, tint = LocalContentColor.current) {
                    Text(stringResource(label))
                }
            }
        }
    }
}

@Composable
private fun ProfileRecovery(refreshing: Boolean, onRetry: () -> Unit) {
    QuizzNoticeBanner(
        tone = QuizzNoticeTone.ERROR,
        icon = QuizzStatusIcon.AUTH,
        actions = {
            QuizzNoticeAction(
                stringResource(R.string.mobile_profile_retry),
                QuizzStatusIcon.SYNC,
                onRetry,
                enabled = !refreshing,
            )
        },
    ) {
        Text(stringResource(R.string.mobile_profile_unavailable), style = MaterialTheme.typography.titleSmall)
        Text(
            stringResource(R.string.mobile_profile_retry_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun ProfileLoading() {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    QuizzMobileSkeleton(stringResource(R.string.mobile_profile_loading), Modifier.fillMaxWidth()) {
        QuizzProfilePanel(colors.surfaceContainer, colors.primaryContainer, shapes.extraLarge) {
            QuizzProfileIdentity(avatar = {
                Box(Modifier.size(QuizzIdentityDimensions.mobileAvatar.dp).quizzSkeleton(CircleShape))
            }) {
                Box(Modifier.fillMaxWidth(0.52f).height(22.dp).quizzSkeleton())
                Box(Modifier.fillMaxWidth(0.79f).height(16.dp).quizzSkeleton())
            }
            Box(Modifier.fillMaxWidth().height(48.dp).quizzSkeleton(shapes.large))
        }
    }
}
