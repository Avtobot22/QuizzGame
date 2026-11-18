package com.quizzgame.core.designsystem.tv.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.ui.LocalQuizzCatalogColors
import com.quizzgame.core.ui.tokens.QuizzCatalogDimensions

@Composable
fun QuizzTvBadge(
    text: String,
    live: Boolean,
    modifier: Modifier = Modifier,
    foreground: ColorSpec? = null,
    background: ColorSpec? = null,
    style: QuizzTvBadgeStyle = QuizzTvBadgeStyle.COMPACT,
) {
    val colors = LocalQuizzCatalogColors.current
    val container = background.themeColor(if (live) colors.liveBadge else colors.badge)
    val emphasized = style == QuizzTvBadgeStyle.EMPHASIZED
    Text(
        text,
        modifier.background(
            container,
            RoundedCornerShape(
                (
                    if (emphasized) {
                        QuizzCatalogDimensions.emphasizedBadgeCornerRadius
                    } else {
                        QuizzCatalogDimensions.badgeCornerRadius
                    }
                    ).dp,
            ),
        ).padding(
            horizontal = if (emphasized) 10.dp else QuizzCatalogDimensions.badgeHorizontalPadding.dp,
            vertical = if (emphasized) 5.dp else QuizzCatalogDimensions.badgeVerticalPadding.dp,
        ),
        color = foreground.readableColor(
            if (live) colors.onLiveBadge else colors.onBadge,
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

/** Высота компактного бейджа для выравнивания текста карточек и их скелетонов. */
@Composable
fun quizzTvCompactBadgeHeight(): Dp = with(LocalDensity.current) {
    MaterialTheme.typography.labelSmall.lineHeight.toDp() + QuizzCatalogDimensions.badgeVerticalPadding.dp * 2
}
