package com.quizzgame.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.dp

@Composable
fun QuizzSkeleton(
    label: String,
    baseColor: Color,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val motionEnabled = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor != 0f
    val phase = skeletonPhase(motionEnabled)
    val appearance = remember(baseColor, highlightColor, phase) { SkeletonAppearance(baseColor, highlightColor, phase) }
    CompositionLocalProvider(LocalSkeletonAppearance provides appearance) {
        Box(
            modifier.clearAndSetSemantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            },
        ) { content() }
    }
}

@Composable
fun Modifier.quizzSkeleton(shape: Shape = RoundedCornerShape(8.dp)): Modifier {
    val appearance = checkNotNull(LocalSkeletonAppearance.current)
    return drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path().apply { addOutline(outline) }
        onDrawBehind {
            clipPath(path) {
                drawRect(appearance.base)
                val band = size.width * 0.65f
                val start = -band + (size.width + band * 2f) * appearance.phase.value
                drawRect(
                    Brush.linearGradient(
                        listOf(appearance.base, appearance.highlight, appearance.base),
                        start = Offset(start, 0f),
                        end = Offset(start + band, size.height * 0.4f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun skeletonPhase(motionEnabled: Boolean): State<Float> {
    if (!motionEnabled) return remember { mutableFloatStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "skeleton")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "skeleton-sweep",
    )
}

private data class SkeletonAppearance(val base: Color, val highlight: Color, val phase: State<Float>)

private val LocalSkeletonAppearance = staticCompositionLocalOf<SkeletonAppearance?> { null }
