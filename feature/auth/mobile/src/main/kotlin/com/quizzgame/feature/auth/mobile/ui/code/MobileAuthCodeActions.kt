package com.quizzgame.feature.auth.mobile.ui.code

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInIntent
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.shared.auth.domain.entity.AuthMethod

@Composable
internal fun MobileAuthCodeActions(
    method: AuthMethod,
    waitMillis: Long,
    canRequest: Boolean,
    onIntent: (MobileSignInIntent) -> Unit,
) {
    val padding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { onIntent(MobileSignInIntent.RequestCode) },
                modifier = Modifier.testTag(MobileSignInTags.RESEND),
                enabled = canRequest && waitMillis == 0L,
                contentPadding = padding,
            ) { Text(stringResource(R.string.auth_resend)) }
            if (waitMillis > 0) ResendTimer(waitMillis)
        }
        TextButton(
            onClick = { onIntent(MobileSignInIntent.Back) },
            modifier = Modifier.testTag(MobileSignInTags.CHANGE_DESTINATION),
            contentPadding = padding,
        ) {
            val label = if (method == AuthMethod.EMAIL) R.string.auth_change_email else R.string.auth_change_phone
            Text(stringResource(label))
        }
    }
}

@Composable
private fun ResendTimer(waitMillis: Long) {
    val seconds = (waitMillis + 999) / 1000
    val clock = stringResource(R.string.auth_timer, seconds / 60, seconds % 60)
    val label = stringResource(R.string.auth_cooldown, seconds)
    Text(
        clock,
        Modifier.testTag(MobileSignInTags.RESEND_TIMER).clearAndSetSemantics { contentDescription = label },
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
