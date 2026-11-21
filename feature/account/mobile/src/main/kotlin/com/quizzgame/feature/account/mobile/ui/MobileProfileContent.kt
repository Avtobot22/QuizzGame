package com.quizzgame.feature.account.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.feature.account.mobile.R
import com.quizzgame.feature.account.mobile.ui.card.ProfileCard
import com.quizzgame.feature.account.mobile.ui.card.ProfileLoading
import com.quizzgame.feature.account.mobile.ui.editor.MobileNicknameEditor
import com.quizzgame.feature.account.mobile.ui.tags.MobileProfileTags
import com.quizzgame.shared.account.presentation.ProfileIntent
import com.quizzgame.shared.account.presentation.ProfileState

@Composable
internal fun MobileProfileContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    QuizzScreenScaffold(
        modifier = Modifier.testTag(MobileProfileTags.SCREEN),
        topBar = { QuizzTopBar(stringResource(R.string.mobile_profile_title)) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = QuizzMobileLayout.contentWidth).fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = QuizzMobileLayout.pageGutter, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (state) {
                    ProfileState.Loading -> ProfileLoading()
                    is ProfileState.Ready -> {
                        ProfileCard(
                            state.profile,
                            state.refreshing,
                            onRetry = { onIntent(ProfileIntent.Retry) },
                            onSignIn = onSignIn,
                            onEditNickname = { onIntent(ProfileIntent.EditNickname) },
                        )
                        if (state.refreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
                        ProfileSignOut(state.profile, onSignOut)
                    }
                }
            }
        }
    }
    (state as? ProfileState.Ready)?.editor?.let { MobileNicknameEditor(it, onIntent) }
}
