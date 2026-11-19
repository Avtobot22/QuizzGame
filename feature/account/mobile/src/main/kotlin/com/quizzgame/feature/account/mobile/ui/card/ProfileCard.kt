package com.quizzgame.feature.account.mobile.ui.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzMobileSkeleton
import com.quizzgame.core.ui.QuizzProfileAvatar
import com.quizzgame.core.ui.QuizzProfilePanel
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.feature.account.mobile.R
import com.quizzgame.feature.account.mobile.ui.tags.MobileProfileTags
import com.quizzgame.shared.splash.domain.entity.AccountProfile

@Composable
internal fun ProfileCard(profile: AccountProfile, refreshing: Boolean, onRetry: () -> Unit, onSignIn: () -> Unit) {
    val user = (profile as? AccountProfile.SignedIn)?.user
    val title = when (profile) {
        AccountProfile.Guest       -> stringResource(R.string.mobile_profile_guest)
        AccountProfile.Unavailable -> stringResource(R.string.mobile_profile_unavailable)
        is AccountProfile.SignedIn -> user?.displayName?.takeIf(String::isNotBlank)
            ?: stringResource(R.string.mobile_profile_member)
    }
    val description = when (profile) {
        AccountProfile.Guest       -> stringResource(R.string.mobile_profile_guest_description)
        AccountProfile.Unavailable -> stringResource(R.string.mobile_profile_retry_description)
        is AccountProfile.SignedIn -> when (user) {
            null -> stringResource(R.string.mobile_profile_retry_description)
            else -> user.email ?: user.phoneNumber ?: stringResource(R.string.mobile_profile_member_description)
        }
    }
    val colors = MaterialTheme.colorScheme
    QuizzProfilePanel(colors.surfaceContainer, colors.primaryContainer, MaterialTheme.shapes.extraLarge) {
        QuizzProfileAvatar(
            user?.displayName,
            colors.primaryContainer,
            colors.onPrimaryContainer,
            MaterialTheme.typography.headlineMedium,
            Modifier.size(72.dp),
        )
        Text(
            title,
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when {
            profile == AccountProfile.Guest -> QuizzButton(
                onSignIn,
                Modifier.fillMaxWidth().testTag(MobileProfileTags.SIGN_IN),
            ) { Text(stringResource(R.string.mobile_profile_sign_in)) }
            user == null -> QuizzButton(onRetry, Modifier.fillMaxWidth(), enabled = !refreshing) {
                Text(stringResource(R.string.mobile_profile_retry))
            }
        }
    }
}

@Composable
internal fun ProfileLoading() {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    QuizzMobileSkeleton(stringResource(R.string.mobile_profile_loading), Modifier.fillMaxWidth()) {
        QuizzProfilePanel(colors.surfaceContainer, colors.primaryContainer, shapes.extraLarge) {
            Box(Modifier.size(72.dp).quizzSkeleton(CircleShape))
            Box(Modifier.fillMaxWidth(0.52f).height(29.dp).quizzSkeleton())
            Box(Modifier.fillMaxWidth(0.79f).height(20.dp).quizzSkeleton())
            Box(Modifier.fillMaxWidth().height(48.dp).quizzSkeleton(shapes.large))
        }
    }
}
