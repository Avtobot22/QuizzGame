package com.quizzgame.core.ui

import android.os.Build
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.PathParser
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

/**
 * One shared brand mark for Mobile and TV, in light/dark Material themes.
 * - The logo uses the same centered vector geometry as the adaptive launcher icon.
 * - The neon halo is static. No repeating logo scale, glow pulse or alpha animation.
 * - The orbit uses ONE smooth vector path with a continuous trailing gradient.
 * - The phase is read in DrawScope, so only the animated canvas needs redrawing.
 */
@Composable
fun QuizzBrandLoading(
    label: String,
    primary: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
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

    val padding = if (expanded) QuizzBrandLoadingDimensions.expandedPadding
        else QuizzBrandLoadingDimensions.compactPadding
    val gap = if (expanded) QuizzBrandLoadingDimensions.expandedGap
        else QuizzBrandLoadingDimensions.compactGap

    BoxWithConstraints(
        modifier.fillMaxSize().padding(padding.dp).semantics(mergeDescendants = true) {
            contentDescription = label
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
        contentAlignment = Alignment.Center,
    ) {
        val maxMark = if (expanded) QuizzBrandLoadingDimensions.expandedMark
            else QuizzBrandLoadingDimensions.compactMark
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QuizzBrandLoadingDimensions.textGap.dp),
            ) {
                title()
                supporting?.invoke()
            }
            LoadingDots(primary)
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
    val glowAlpha = if (primary.luminance() > 0.48f) 0.49f else 0.26f
    val logoPaths = remember {
        listOf(
            PathParser().parsePathString(Q_RING_PATH).toPath(),
            PathParser().parsePathString(Q_TAIL_PATH).toPath(),
        )
    }

    Box(modifier, contentAlignment = Alignment.Center) {
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

            // One path per pass: no abrupt join between separately drawn arc segments.
            drawPath(
                path = trail,
                brush = Brush.linearGradient(
                    colors = listOf(primary.copy(alpha = 0.0f), primary.copy(alpha = 0.16f)),
                    start = tail,
                    end = head,
                ),
                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = trail,
                brush = Brush.linearGradient(
                    colors = listOf(primary.copy(alpha = 0.06f), primary.copy(alpha = 0.98f)),
                    start = tail,
                    end = head,
                ),
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawCircle(accent.copy(alpha = 0.20f), radius = 5.5.dp.toPx(), center = head)
            drawCircle(accent, radius = 2.6.dp.toPx(), center = head)
        }

        // Android 12+: the vector is rasterized by Compose at display resolution and
        // blurred only for its static neon halo. This is NOT a pre-rendered bitmap GIF.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Image(
                painter = vector,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
                    .blur(13.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .graphicsLayer { alpha = glowAlpha },
                colorFilter = ColorFilter.tint(primary),
            )
            Image(
                painter = vector,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
                    .blur(4.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .graphicsLayer { alpha = glowAlpha * 0.55f },
                colorFilter = ColorFilter.tint(primary),
            )
        } else {
            // Older Android: three translucent vector outlines simulate a soft halo
            // without relying on the unsupported pre-31 RenderEffect blur.
            Canvas(Modifier.fillMaxSize()) {
                val factor = size.minDimension / VECTOR_VIEWPORT
                withTransform({ scale(factor, factor, pivot = Offset.Zero) }) {
                    for (path in logoPaths) {
                        drawPath(path, primary.copy(alpha = 0.075f), style = Stroke(width = 11f, join = StrokeJoin.Round))
                        drawPath(path, primary.copy(alpha = 0.105f), style = Stroke(width = 6f, join = StrokeJoin.Round))
                        drawPath(path, primary.copy(alpha = 0.14f), style = Stroke(width = 2.5f, join = StrokeJoin.Round))
                    }
                }
            }
        }

        Image(
            painter = vector,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(primary),
        )
    }
}

private fun orbitPoint(center: Offset, radius: Float, angleDegrees: Float): Offset {
    val radians = angleDegrees * PI / 180.0
    return center + Offset(cos(radians).toFloat(), sin(radians).toFloat()) * radius
}

@Composable
private fun LoadingDots(color: Color) {
    Canvas(Modifier.size(QuizzBrandLoadingDimensions.dotsWidth.dp, QuizzBrandLoadingDimensions.dotsHeight.dp)) {
        val alpha = floatArrayOf(0.26f, 0.43f, 0.68f)
        repeat(3) { index ->
            drawCircle(
                color = color.copy(alpha = alpha[index]),
                radius = 2.7.dp.toPx(),
                center = Offset(center.x + (index - 1) * 16.dp.toPx(), center.y),
            )
        }
    }
}

// Keep these paths in sync with ic_quizz(_monochrome).xml and design/logo-q.svg.
private const val Q_RING_PATH = "M54,26 C69.46397,26 82,38.53603 82,54 C82,69.46397 69.46397,82 54,82 C38.53603,82 26,69.46397 26,54 C26,38.53603 38.53603,26 54,26 Z M54,38 C45.16344,38 38,45.16344 38,54 C38,62.83656 45.16344,70 54,70 C62.83656,70 70,62.83656 70,54 C70,45.16344 62.83656,38 54,38 Z"
private const val Q_TAIL_PATH = "M64,68 L73,61 L84,76 L75,83 Z"
private const val VECTOR_VIEWPORT = 108f
private const val ORBIT_RADIUS_FRACTION = 0.385f
private const val TRAIL_SWEEP_DEGREES = 66f
private const val ENTRANCE_MS = 620
private const val MOBILE_CYCLE_MS = 4800
private const val TV_CYCLE_MS = 6500
