package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.ui.tokens.QuizzTvLayout

@Composable
fun QuizzTvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: QuizzTvButtonStyle = QuizzTvButtonStyle.PRIMARY,
    tone: QuizzTvButtonTone = QuizzTvButtonTone.DEFAULT,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(QuizzTvLayout.cornerRadius.dp)
    val primary = style == QuizzTvButtonStyle.PRIMARY
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ButtonDefaults.shape(shape = shape),
        colors = if (tone == QuizzTvButtonTone.DANGER) dangerButtonColors(style) else standardButtonColors(style),
        scale = ButtonDefaults.scale(focusedScale = 1.025f),
        border = ButtonDefaults.border(
            border = if (style == QuizzTvButtonStyle.SECONDARY) {
                Border(
                    BorderStroke(
                        1.dp,
                        (if (tone == QuizzTvButtonTone.DANGER) colors.error else colors.border).copy(alpha = 0.4f),
                    ),
                    shape = shape,
                )
            } else {
                Border.None
            },
            focusedBorder = if (primary) {
                Border.None
            } else {
                Border(
                    BorderStroke(2.dp, if (tone == QuizzTvButtonTone.DANGER) colors.error else colors.onSurface),
                    shape = shape,
                )
            },
            disabledBorder = Border.None,
            focusedDisabledBorder = Border.None,
        ),
        glow = ButtonDefaults.glow(),
        contentPadding = PaddingValues(horizontal = QuizzTvLayout.buttonHorizontalPadding.dp, vertical = 10.dp),
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
            Row(
                Modifier.heightIn(min = (QuizzTvLayout.buttonHeight - 20f).dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
private fun standardButtonColors(style: QuizzTvButtonStyle): ButtonColors {
    val colors = MaterialTheme.colorScheme
    val primary = style == QuizzTvButtonStyle.PRIMARY
    return ButtonDefaults.colors(
        containerColor = when (style) {
            QuizzTvButtonStyle.PRIMARY -> colors.primaryContainer
            QuizzTvButtonStyle.SECONDARY -> colors.surfaceVariant.copy(alpha = 0.45f)
            QuizzTvButtonStyle.QUIET -> Color.Transparent
        },
        contentColor = if (primary) colors.onPrimaryContainer else colors.onSurface,
        focusedContainerColor = if (primary) colors.primary else colors.surfaceVariant,
        focusedContentColor = if (primary) colors.onPrimary else colors.onSurface,
        pressedContainerColor = if (primary) colors.primaryContainer else colors.surfaceVariant,
        pressedContentColor = if (primary) colors.onPrimaryContainer else colors.onSurface,
        disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.25f),
        disabledContentColor = colors.onSurface.copy(alpha = 0.38f),
    )
}

@Composable
private fun dangerButtonColors(style: QuizzTvButtonStyle): ButtonColors {
    val colors = MaterialTheme.colorScheme
    val primary = style == QuizzTvButtonStyle.PRIMARY
    return ButtonDefaults.colors(
        containerColor = when (style) {
            QuizzTvButtonStyle.PRIMARY -> colors.errorContainer
            QuizzTvButtonStyle.SECONDARY -> colors.errorContainer.copy(alpha = 0.25f)
            QuizzTvButtonStyle.QUIET -> Color.Transparent
        },
        contentColor = if (primary) colors.onErrorContainer else colors.error,
        focusedContainerColor = if (primary) colors.error else colors.errorContainer,
        focusedContentColor = if (primary) colors.onError else colors.onErrorContainer,
        pressedContainerColor = colors.errorContainer,
        pressedContentColor = colors.onErrorContainer,
        disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.25f),
        disabledContentColor = colors.onSurface.copy(alpha = 0.38f),
    )
}
