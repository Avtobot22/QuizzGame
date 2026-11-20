package com.quizzgame.feature.account.mobile.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.account.mobile.R
import com.quizzgame.feature.account.mobile.ui.tags.MobileProfileTags
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun ProfileSignOut(profile: PlayerProfile, onSignOut: () -> Unit) {
    if (profile !is PlayerProfile.SignedIn) return
    val color = MaterialTheme.colorScheme.error
    QuizzButton(
        onSignOut,
        Modifier.fillMaxWidth().testTag(MobileProfileTags.SIGN_OUT),
        style = QuizzButtonStyle.TEXT,
    ) {
        QuizzIconLabel(UiR.drawable.ic_quizz_logout, tint = color) {
            Text(stringResource(R.string.mobile_profile_sign_out), color = color)
        }
    }
}
