package com.quizzgame.feature.dashboard.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account

internal class MobileAccountPreviewProvider : PreviewParameterProvider<Account> {
    override val values = sequenceOf(Account.Loading, Account.Guest, Account.SignedIn("Александр"), Account.Unavailable)
}
