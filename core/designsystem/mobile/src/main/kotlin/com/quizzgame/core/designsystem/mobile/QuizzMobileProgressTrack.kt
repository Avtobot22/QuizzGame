package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Сегменты показывают долю прохождения с точным заполнением внутри каждого сегмента. */
@Composable
fun QuizzMobileProgressTrack(progress: Float, modifier: Modifier = Modifier, height: Dp = 10.dp) {
    val colors = MaterialTheme.colorScheme
    val fraction = progress.coerceIn(0f, 1f)
    val fill = Brush.verticalGradient(listOf(lerp(colors.primary, colors.onPrimary, 0.22f), colors.primary))
    Row(
        modifier.fillMaxWidth().height(height).progressSemantics(fraction),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(SEGMENT_COUNT) { index ->
            val segmentProgress = (fraction * SEGMENT_COUNT - index).coerceIn(0f, 1f)
            Box(
                Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(colors.surfaceContainerHighest),
            ) {
                Box(Modifier.fillMaxWidth(segmentProgress).fillMaxHeight().background(fill, CircleShape))
            }
        }
    }
}

private const val SEGMENT_COUNT = 5
