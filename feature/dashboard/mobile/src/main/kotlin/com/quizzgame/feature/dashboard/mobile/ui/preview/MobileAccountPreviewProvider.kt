package com.quizzgame.feature.dashboard.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account

internal class MobileAccountPreviewProvider : PreviewParameterProvider<MobileAccountPreviewState> {
    override val values = sequenceOf(false, true).flatMap { library ->
        sequenceOf(Account.Loading, Account.Guest, Account.SignedIn("Александр"), Account.Unavailable).map {
            MobileAccountPreviewState(it, library)
        }
    }
}
