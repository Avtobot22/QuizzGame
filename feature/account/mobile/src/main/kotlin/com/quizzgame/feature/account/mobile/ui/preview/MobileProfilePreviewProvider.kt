package com.quizzgame.feature.account.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.feature.account.mobile.presentation.MobileProfileState
import com.quizzgame.shared.splash.domain.entity.AccountProfile
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import java.time.Instant
import java.util.UUID

internal class MobileProfilePreviewProvider : PreviewParameterProvider<MobileProfileState> {
    override val values = sequenceOf(
        MobileProfileState.Loading,
        MobileProfileState.Ready(AccountProfile.Guest),
        MobileProfileState.Ready(AccountProfile.SignedIn(sampleUser)),
        MobileProfileState.Ready(AccountProfile.SignedIn(null)),
        MobileProfileState.Ready(AccountProfile.Unavailable),
        MobileProfileState.Ready(AccountProfile.Unavailable, refreshing = true),
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
