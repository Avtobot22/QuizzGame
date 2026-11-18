package com.quizzgame.feature.feed.mobile.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun MobileFeedSectionHeader(title: String?, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        title?.let {
            Text(it, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        }
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
