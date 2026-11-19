package com.quizzgame.feature.account.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.feature.account.tv.presentation.TvProfileState
import com.quizzgame.shared.splash.domain.entity.AccountProfile
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import java.time.Instant
import java.util.UUID

internal class TvProfilePreviewProvider : PreviewParameterProvider<TvProfileState> {
    private val sample = CurrentUser(UUID(0, 0), Instant.EPOCH, null, null, "Александр", null, emptyList())
    override val values = sequenceOf(
        TvProfileState.Loading,
        TvProfileState.Loaded(AccountProfile.Guest),
        TvProfileState.Loaded(AccountProfile.SignedIn(sample)),
        TvProfileState.Loaded(AccountProfile.SignedIn(null)),
        TvProfileState.Loaded(AccountProfile.Unavailable),
        TvProfileState.Loaded(AccountProfile.Unavailable, refreshing = true),
    )
}
