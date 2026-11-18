package com.quizzgame.feature.feed.mobile.ui.status

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzStatusContent
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.feature.feed.mobile.R

@Composable
internal fun MobileFeedEmpty(onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
        QuizzStatusContent(icon = QuizzStatusIcon.CATALOG, actions = {
            QuizzButton(onRefresh) { Text(stringResource(R.string.feed_refresh)) }
        }) {
            Text(
                stringResource(R.string.feed_empty),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.feed_empty_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
