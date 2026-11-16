package com.quizzgame.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun QuizzSceneIllustration(
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    symbol: @Composable () -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            scale(size.width / VIEWPORT, size.height / VIEWPORT, pivot = Offset.Zero) {
                drawCircle(primary.copy(alpha = 0.06f), 96f, Offset(120f, 120f))
                drawCircle(primary.copy(alpha = 0.13f), 94f, Offset(120f, 120f), style = Stroke(1f))
                rotate(-16f, Offset(120f, 120f)) {
                    drawRoundRect(
                        primary.copy(alpha = 0.12f),
                        Offset(53f, 53f),
                        Size(114f, 136f),
                        CornerRadius(18f),
                    )
                    drawRoundRect(
                        primary.copy(alpha = 0.26f),
                        Offset(53f, 53f),
                        Size(114f, 136f),
                        CornerRadius(18f),
                        style = Stroke(1.5f),
                    )
                }
                drawRoundRect(primary.copy(alpha = 0.18f), Offset(63f, 52f), Size(114f, 136f), CornerRadius(18f))
                drawRoundRect(
                    primary.copy(alpha = 0.65f),
                    Offset(63f, 52f),
                    Size(114f, 136f),
                    CornerRadius(18f),
                    style = Stroke(2f),
                )
                drawLine(primary.copy(alpha = 0.45f), Offset(93f, 148f), Offset(147f, 148f), 4f, StrokeCap.Round)
                drawLine(primary.copy(alpha = 0.2f), Offset(103f, 160f), Offset(137f, 160f), 3f, StrokeCap.Round)
                drawCircle(accent.copy(alpha = 0.75f), 5f, Offset(199f, 77f))
                drawCircle(primary.copy(alpha = 0.45f), 3f, Offset(40f, 155f))
                val star = Path().apply {
                    moveTo(53f, 39f)
                    lineTo(57f, 49f)
                    lineTo(67f, 53f)
                    lineTo(57f, 57f)
                    lineTo(53f, 67f)
                    lineTo(49f, 57f)
                    lineTo(39f, 53f)
                    lineTo(49f, 49f)
                    close()
                }
                drawPath(star, accent.copy(alpha = 0.8f))
            }
        }
        Box(
            Modifier.matchParentSize().graphicsLayer { translationY = -size.height * SYMBOL_OFFSET_FRACTION },
            contentAlignment = Alignment.Center,
        ) { symbol() }
    }
}

private const val VIEWPORT = 240f
private const val SYMBOL_OFFSET_FRACTION = 14f / VIEWPORT
