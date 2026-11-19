package com.quizzgame.core.designsystem.mobile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.tokens.QuizzProviderColors

/** Кнопка внешнего провайдера с оригинальным цветом логотипа и подписью вызывающего экрана. */
@Composable
fun QuizzProviderButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colors = QuizzProviderColors
    val container = if (dark) colors.DarkSurface else colors.NeutralSurface
    OutlinedButton(
        onClick,
        modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = container,
            contentColor = if (dark) colors.DarkText else colors.NeutralText,
            disabledContainerColor = container,
        ),
        border = if (dark) BorderStroke(1.dp, colors.DarkOutline) else null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    ) {
        QuizzIconLabel(icon, gap = 10.dp) { Text(text) }
    }
}
