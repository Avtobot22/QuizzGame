package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.tokens.QuizzDimensions

@Composable
fun QuizzButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: QuizzButtonStyle = QuizzButtonStyle.FILLED,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val target = modifier.sizeIn(
        minWidth = QuizzDimensions.mobileMinTouchDp.dp,
        minHeight = QuizzDimensions.mobileMinTouchDp.dp,
    )
    val shape = MaterialTheme.shapes.large
    val padding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    when (style) {
        QuizzButtonStyle.FILLED -> Button(
            onClick,
            target,
            enabled,
            shape = shape,
            contentPadding = padding,
            content = content,
        )
        QuizzButtonStyle.TONAL -> FilledTonalButton(
            onClick,
            target,
            enabled,
            shape = shape,
            contentPadding = padding,
            content = content,
        )
        QuizzButtonStyle.OUTLINED -> OutlinedButton(
            onClick,
            target,
            enabled,
            shape = shape,
            contentPadding = padding,
            content = content,
        )
        QuizzButtonStyle.ELEVATED -> ElevatedButton(
            onClick,
            target,
            enabled,
            shape = shape,
            contentPadding = padding,
            content = content,
        )
        QuizzButtonStyle.TEXT -> TextButton(
            onClick,
            target,
            enabled,
            shape = shape,
            contentPadding = padding,
            content = content,
        )
    }
}
