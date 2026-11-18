package com.quizzgame.core.designsystem.mobile.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.ui.LocalQuizzCatalogColors
import com.quizzgame.core.ui.tokens.QuizzCatalogDimensions

@Composable
fun QuizzMobileBadge(
    text: String,
    modifier: Modifier = Modifier,
    foreground: ColorSpec? = null,
    background: ColorSpec? = null,
    live: Boolean = false,
) {
    val colors = LocalQuizzCatalogColors.current
    val container = background.themeColor(if (live) colors.liveBadge else colors.badge)
    Text(
        text,
        modifier.background(
            container,
            RoundedCornerShape(QuizzCatalogDimensions.badgeCornerRadius.dp),
        ).padding(
            horizontal = QuizzCatalogDimensions.badgeHorizontalPadding.dp,
            vertical = QuizzCatalogDimensions.badgeVerticalPadding.dp,
        ),
        color = foreground.readableColor(if (live) colors.onLiveBadge else colors.onBadge, container),
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
