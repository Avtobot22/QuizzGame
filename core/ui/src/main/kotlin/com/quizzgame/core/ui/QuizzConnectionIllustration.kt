package com.quizzgame.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

@Composable
fun QuizzConnectionIllustration(primary: Color, accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        scale(size.width / 240f, size.height / 240f, pivot = Offset.Zero) {
            drawCircle(primary.copy(alpha = 0.05f), 100f, Offset(120f, 120f))
            drawCircle(primary.copy(alpha = 0.12f), 99f, Offset(120f, 120f), style = Stroke(1f))
            drawRoundRect(primary.copy(alpha = 0.12f), Offset(24f, 51f), Size(172f, 110f), CornerRadius(12f))
            drawRoundRect(
                primary.copy(alpha = 0.7f),
                Offset(24f, 51f),
                Size(172f, 110f),
                CornerRadius(12f),
                style = Stroke(2f),
            )
            drawLine(primary.copy(alpha = 0.5f), Offset(88f, 177f), Offset(132f, 177f), 3f)
            drawLine(primary.copy(alpha = 0.5f), Offset(110f, 162f), Offset(110f, 177f), 3f)
            drawRoundRect(primary.copy(alpha = 0.9f), Offset(46f, 78f), Size(58f, 28f), CornerRadius(6f))
            drawRoundRect(accent.copy(alpha = 0.7f), Offset(114f, 78f), Size(58f, 28f), CornerRadius(6f))
            drawRoundRect(accent.copy(alpha = 0.3f), Offset(46f, 116f), Size(58f, 24f), CornerRadius(6f))
            drawRoundRect(primary.copy(alpha = 0.3f), Offset(114f, 116f), Size(58f, 24f), CornerRadius(6f))
            drawRoundRect(primary.copy(alpha = 0.95f), Offset(151f, 108f), Size(54f, 94f), CornerRadius(12f))
            drawRoundRect(accent.copy(alpha = 0.25f), Offset(157f, 119f), Size(42f, 65f), CornerRadius(5f))
            drawLine(accent, Offset(169f, 192f), Offset(187f, 192f), 2f)
            drawCircle(accent.copy(alpha = 0.8f), 4f, Offset(39f, 190f))
            drawCircle(primary.copy(alpha = 0.65f), 3f, Offset(203f, 47f))
        }
    }
}
