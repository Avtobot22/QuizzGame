package com.quizzgame.feature.account.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.shared.account.domain.entity.PlayerProfile
import com.quizzgame.shared.account.presentation.NicknameEditorState
import com.quizzgame.shared.account.presentation.ProfileState
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import java.time.Instant
import java.util.UUID

internal class TvProfilePreviewProvider : PreviewParameterProvider<ProfileState> {
    private val sample = CurrentUser(UUID(0, 0), Instant.EPOCH, null, null, "Александр", null, emptyList())
    override val values = sequenceOf(
        ProfileState.Loading,
        ProfileState.Ready(PlayerProfile.Guest),
        ProfileState.Ready(PlayerProfile.SignedIn(1, sample)),
        ProfileState.Ready(PlayerProfile.SignedIn(1, sample.copy(displayName = null))),
        ProfileState.Ready(PlayerProfile.SignedIn(1, sample), editor = NicknameEditorState("QuizOtter-482")),
        ProfileState.Ready(
            PlayerProfile.SignedIn(1, sample),
            editor = NicknameEditorState("QuizOtter-482", saving = true),
        ),
        ProfileState.Ready(
            PlayerProfile.SignedIn(1, sample),
            editor = NicknameEditorState("QuizOtter-482", issue = NicknameEditorState.Issue.SAVE_FAILURE),
        ),
        ProfileState.Ready(
            PlayerProfile.SignedIn(1, sample),
            editor = NicknameEditorState("x".repeat(25), issue = NicknameEditorState.Issue.INVALID_NAME),
        ),
        ProfileState.Ready(PlayerProfile.SignedIn(1, null)),
        ProfileState.Ready(PlayerProfile.Unavailable),
        ProfileState.Ready(PlayerProfile.Unavailable, refreshing = true),
    )
}
