package com.quizzgame.core.designsystem.tv.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.media.ColorSpec

@Composable
fun QuizzTvBadge(
    text: String,
    live: Boolean,
    modifier: Modifier = Modifier,
    foreground: ColorSpec? = null,
    background: ColorSpec? = null,
    style: QuizzTvBadgeStyle = QuizzTvBadgeStyle.COMPACT,
) {
    val colors = MaterialTheme.colorScheme
    val container = background.themeColor(if (live) colors.tertiary else colors.secondaryContainer)
    val emphasized = style == QuizzTvBadgeStyle.EMPHASIZED
    Text(
        text,
        modifier.background(container, RoundedCornerShape(if (emphasized) 6.dp else 5.dp)).padding(
            horizontal = if (emphasized) 10.dp else 8.dp,
            vertical = if (emphasized) 5.dp else 4.dp,
        ),
        color = foreground.readableColor(
            if (live) colors.onTertiary else colors.onSecondaryContainer,
            container,
        ),
        style = if (emphasized) {
            MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        } else {
            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal)
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
