package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.ui.tokens.QuizzDimensions

@Composable
fun QuizzTvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val focusBorder = Border(
        BorderStroke(QuizzDimensions.tvFocusOutlineDp.dp, colors.border),
        shape = CircleShape,
    )
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.colors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            focusedContainerColor = colors.primary,
            focusedContentColor = colors.onPrimary,
            pressedContainerColor = colors.secondaryContainer,
            pressedContentColor = colors.onSecondaryContainer,
            disabledContainerColor = colors.surfaceVariant,
            disabledContentColor = colors.onSurfaceVariant,
        ),
        scale = ButtonDefaults.scale(focusedScale = QuizzDimensions.tvFocusScaleButton),
        border = ButtonDefaults.border(
            focusedBorder = focusBorder,
            focusedDisabledBorder = focusBorder,
        ),
        contentPadding = PaddingValues(horizontal = 42.dp, vertical = 24.dp),
    ) {
        CompositionLocalProvider(LocalTextStyle provides TvComponentType.button) { content() }
    }
}
