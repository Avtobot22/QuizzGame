package com.quizzgame.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.tokens.QuizzBrandLoadingDimensions
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QuizzBrandLoading(
    label: String,
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    size: QuizzBrandLoadingSize = QuizzBrandLoadingSize.COMPACT,
) {
    val expanded = size == QuizzBrandLoadingSize.EXPANDED
    val motionEnabled = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor != 0f
    val phase = brandPhase(motionEnabled, expanded)
    val entrance = remember { Animatable(0f) }

    LaunchedEffect(motionEnabled) {
        if (motionEnabled) {
            entrance.animateTo(1f, tween(ENTRANCE_MS, easing = FastOutSlowInEasing))
        } else {
            entrance.snapTo(1f)
        }
    }

    val padding = if (expanded) {
        QuizzBrandLoadingDimensions.expandedPadding
    } else {
        QuizzBrandLoadingDimensions.compactPadding
    }
    val gap = if (expanded) {
        QuizzBrandLoadingDimensions.expandedGap
    } else {
        QuizzBrandLoadingDimensions.compactGap
    }

    BoxWithConstraints(
        modifier.fillMaxSize().padding(padding.dp).semantics(mergeDescendants = true) {
            contentDescription = label
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
        contentAlignment = Alignment.Center,
    ) {
        val maxMark = if (expanded) {
            QuizzBrandLoadingDimensions.expandedMark
        } else {
            QuizzBrandLoadingDimensions.compactMark
        }
        val markSize = minOf(maxMark.dp, maxHeight * 0.44f, maxWidth * 0.68f)

        Column(
            modifier = Modifier.graphicsLayer {
                val p = if (motionEnabled) entrance.value else 1f
                alpha = 0.3f + 0.7f * p
                translationY = 8.dp.toPx() * (1f - p)
            }.clearAndSetSemantics {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(gap.dp),
        ) {
            BrandMark(primary, accent, phase, Modifier.size(markSize))
            if (title != null || supporting != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(QuizzBrandLoadingDimensions.textGap.dp),
                ) {
                    title?.invoke()
                    supporting?.invoke()
                }
            }
        }
    }
}

@Composable
private fun brandPhase(motionEnabled: Boolean, expanded: Boolean): State<Float> {
    if (!motionEnabled) return remember { mutableFloatStateOf(0.25f) }
    val transition = rememberInfiniteTransition(label = "brand-loading")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (expanded) TV_CYCLE_MS else MOBILE_CYCLE_MS, easing = LinearEasing),
        ),
        label = "brand-orbit",
    )
}

@Composable
private fun BrandMark(primary: Color, accent: Color, phase: State<Float>, modifier: Modifier = Modifier) {
    val vector = painterResource(R.drawable.ic_quizz_monochrome)

    Box(modifier, contentAlignment = Alignment.Center) {
        BrandOrbit(primary, accent, phase)

        Image(
            painter = vector,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(primary),
        )
    }
}

@Composable
private fun BrandOrbit(primary: Color, accent: Color, phase: State<Float>) {
    Canvas(Modifier.fillMaxSize()) {
        val orbit = size.minDimension * ORBIT_RADIUS_FRACTION
        val bounds = Rect(center - Offset(orbit, orbit), center + Offset(orbit, orbit))
        val track = Path().apply { addOval(bounds) }
        // A closed anti-aliased VECTOR path, not a polygonal ring or a raster bitmap.
        drawPath(track, primary.copy(alpha = 0.16f), style = Stroke(width = 1.05.dp.toPx()))

        val headAngle = phase.value * 360f - 90f
        val tailAngle = headAngle - TRAIL_SWEEP_DEGREES
        val trail = Path().apply { addArc(bounds, tailAngle, TRAIL_SWEEP_DEGREES) }
        val head = orbitPoint(center, orbit, headAngle)
        val tail = orbitPoint(center, orbit, tailAngle)

        drawPath(
            path = trail,
            brush = Brush.linearGradient(
                colors = listOf(primary.copy(alpha = 0.06f), primary.copy(alpha = 0.98f)),
                start = tail,
                end = head,
            ),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawCircle(accent, radius = 2.6.dp.toPx(), center = head)
    }
}

private fun orbitPoint(center: Offset, radius: Float, angleDegrees: Float): Offset {
    val radians = angleDegrees * PI / HALF_TURN_DEGREES
    return center + Offset(cos(radians).toFloat(), sin(radians).toFloat()) * radius
}

private const val ORBIT_RADIUS_FRACTION = 0.385f
private const val TRAIL_SWEEP_DEGREES = 66f
private const val ENTRANCE_MS = 620
private const val MOBILE_CYCLE_MS = 4800
private const val TV_CYCLE_MS = 6500

private const val HALF_TURN_DEGREES = 180.0
