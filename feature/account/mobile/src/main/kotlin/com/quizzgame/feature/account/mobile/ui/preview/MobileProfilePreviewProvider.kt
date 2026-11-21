package com.quizzgame.feature.account.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.presentation.NicknameEditorState
import com.quizzgame.shared.account.presentation.ProfileState
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import java.time.Instant
import java.util.UUID

internal class MobileProfilePreviewProvider : PreviewParameterProvider<ProfileState> {
    override val values = sequenceOf(
        ProfileState.Loading,
        ProfileState.Ready(PlayerProfile.Guest),
        ProfileState.Ready(PlayerProfile.SignedIn(1, sampleUser)),
        ProfileState.Ready(PlayerProfile.SignedIn(1, sampleUser.copy(displayName = null))),
        ProfileState.Ready(PlayerProfile.SignedIn(1, sampleUser), editor = NicknameEditorState("QuizOtter-482")),
        ProfileState.Ready(
            PlayerProfile.SignedIn(1, sampleUser),
            editor = NicknameEditorState("QuizOtter-482", saving = true),
        ),
        ProfileState.Ready(
            PlayerProfile.SignedIn(1, sampleUser),
            editor = NicknameEditorState("QuizOtter-482", issue = NicknameEditorState.Issue.SAVE_FAILURE),
        ),
        ProfileState.Ready(
            PlayerProfile.SignedIn(1, sampleUser),
            editor = NicknameEditorState("x".repeat(25), issue = NicknameEditorState.Issue.INVALID_NAME),
        ),
        ProfileState.Ready(PlayerProfile.SignedIn(1, null)),
        ProfileState.Ready(PlayerProfile.Unavailable),
        ProfileState.Ready(PlayerProfile.Unavailable, refreshing = true),
    )
}

private val sampleUser = CurrentUser(
    UUID(0, 0),
    Instant.EPOCH,
    "alex@example.test",
    null,
    "Александр",
    null,
    emptyList(),
)
