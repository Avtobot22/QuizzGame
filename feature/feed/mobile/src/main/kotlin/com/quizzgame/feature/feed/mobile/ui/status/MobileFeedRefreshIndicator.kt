package com.quizzgame.feature.feed.mobile.ui.status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileSkeleton
import com.quizzgame.core.designsystem.mobile.QuizzSkeletonLine
import com.quizzgame.feature.feed.mobile.R
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileFeedRefreshIndicator(refreshing: Boolean, fraction: Float, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(
        when {
            refreshing -> R.string.feed_refreshing
            fraction >= 1f -> R.string.feed_release_refresh
            else -> R.string.feed_pull_refresh
        },
    )
    Surface(
        modifier,
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    painterResource(UiR.drawable.ic_quizz_refresh),
                    null,
                    Modifier.size(18.dp).rotate(fraction.coerceIn(0f, 1f) * 180f),
                    tint = colors.primary,
                )
                Text(label, style = MaterialTheme.typography.labelMedium, color = colors.primary)
            }
            if (refreshing) {
                QuizzMobileSkeleton(label) { QuizzSkeletonLine(Modifier.width(120.dp).height(3.dp)) }
            }
        }
    }
}
