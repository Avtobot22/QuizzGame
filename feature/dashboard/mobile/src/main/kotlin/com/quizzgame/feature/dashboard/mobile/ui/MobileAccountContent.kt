package com.quizzgame.feature.dashboard.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzAccountSkeleton
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.feature.dashboard.mobile.R
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import com.quizzgame.feature.dashboard.mobile.presentation.MobileAccountIntent
import com.quizzgame.feature.dashboard.mobile.ui.tags.MobileAccountTags

@Composable
internal fun MobileAccountContent(
    account: Account,
    library: Boolean,
    onIntent: (MobileAccountIntent) -> Unit,
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(if (library) R.string.account_library else R.string.account_profile)
    QuizzScreenScaffold(modifier, topBar = { QuizzTopBar(title) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)
                .testTag(if (library) MobileAccountTags.LIBRARY else MobileAccountTags.PROFILE),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when (val current = account) {
                Account.Loading -> QuizzAccountSkeleton(title)
                Account.Guest -> {
                    Text(stringResource(if (library) R.string.library_guest else R.string.profile_guest))
                    QuizzButton(onSignIn) { Text(stringResource(R.string.account_sign_in)) }
                }
                is Account.SignedIn -> {
                    Text(
                        current.displayName ?: stringResource(R.string.account_signed_in),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    if (library) {
                        Text(stringResource(R.string.library_pending))
                    } else {
                        QuizzButton(onLogout) { Text(stringResource(R.string.account_logout)) }
                    }
                }
                Account.Unavailable -> {
                    Text(stringResource(R.string.account_unavailable))
                    QuizzButton({ onIntent(MobileAccountIntent.RETRY) }) {
                        Text(stringResource(R.string.account_retry))
                    }
                }
            }
        }
    }
}
