package com.quizzgame.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Выравнивает декоративную иконку и видимую подпись по центру. */
@Composable
fun QuizzIconLabel(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    gap: Dp = 8.dp,
    tint: Color = Color.Unspecified,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    label: @Composable () -> Unit,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(gap, alignment),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            colorFilter = tint.takeIf { it != Color.Unspecified }?.let { ColorFilter.tint(it) },
        )
        label()
    }
}
