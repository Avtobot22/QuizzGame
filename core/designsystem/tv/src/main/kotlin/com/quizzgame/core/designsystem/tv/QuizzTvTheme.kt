package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Shapes
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import com.quizzgame.core.ui.LocalQuizzGameColors
import com.quizzgame.core.ui.QuizzGameColors
import com.quizzgame.core.ui.tokens.QuizzShape
import com.quizzgame.core.ui.tokens.QuizzTypography

private val TvDarkColors = darkColorScheme(
    primary = TvDarkPalette.primary,
    onPrimary = TvDarkPalette.onPrimary,
    primaryContainer = TvDarkPalette.primaryContainer,
    onPrimaryContainer = TvDarkPalette.onPrimaryContainer,
    inversePrimary = TvDarkPalette.inversePrimary,
    secondary = TvDarkPalette.secondary,
    onSecondary = TvDarkPalette.onSecondary,
    secondaryContainer = TvDarkPalette.secondaryContainer,
    onSecondaryContainer = TvDarkPalette.onSecondaryContainer,
    tertiary = TvDarkPalette.tertiary,
    onTertiary = TvDarkPalette.onTertiary,
    tertiaryContainer = TvDarkPalette.tertiaryContainer,
    onTertiaryContainer = TvDarkPalette.onTertiaryContainer,
    background = TvDarkPalette.background,
    onBackground = TvDarkPalette.onBackground,
    surface = TvDarkPalette.surface,
    onSurface = TvDarkPalette.onSurface,
    surfaceVariant = TvDarkPalette.surfaceVariant,
    onSurfaceVariant = TvDarkPalette.onSurfaceVariant,
    surfaceTint = TvDarkPalette.surfaceTint,
    inverseSurface = TvDarkPalette.inverseSurface,
    inverseOnSurface = TvDarkPalette.inverseOnSurface,
    error = TvDarkPalette.error,
    onError = TvDarkPalette.onError,
    errorContainer = TvDarkPalette.errorContainer,
    onErrorContainer = TvDarkPalette.onErrorContainer,
    border = TvDarkPalette.border,
    borderVariant = TvDarkPalette.borderVariant,
    scrim = TvDarkPalette.scrim,
)

private val TvDarkGameColors = QuizzGameColors(
    answerCorrect = GameTvDarkPalette.answerCorrect,
    onAnswerCorrect = GameTvDarkPalette.onAnswerCorrect,
    answerWrong = GameTvDarkPalette.answerWrong,
    onAnswerWrong = GameTvDarkPalette.onAnswerWrong,
    answerSelected = GameTvDarkPalette.answerSelected,
    onAnswerSelected = GameTvDarkPalette.onAnswerSelected,
    timerAccent = GameTvDarkPalette.timerAccent,
    onTimerAccent = GameTvDarkPalette.onTimerAccent,
    streak = GameTvDarkPalette.streak,
    onStreak = GameTvDarkPalette.onStreak,
    scoreHighlight = GameTvDarkPalette.scoreHighlight,
    onScoreHighlight = GameTvDarkPalette.onScoreHighlight,
    answerIdle = GameTvDarkPalette.answerIdle,
    onAnswerIdle = GameTvDarkPalette.onAnswerIdle,
)

private val TvTypography = Typography(
    displayLarge = QuizzTypography.displayLarge,
    displayMedium = QuizzTypography.displayMedium,
    displaySmall = QuizzTypography.displaySmall,
    headlineLarge = QuizzTypography.headlineLarge,
    headlineMedium = QuizzTypography.headlineMedium,
    headlineSmall = QuizzTypography.headlineSmall,
    titleLarge = QuizzTypography.titleLarge,
    titleMedium = QuizzTypography.titleMedium,
    titleSmall = QuizzTypography.titleSmall,
    bodyLarge = QuizzTypography.bodyLarge,
    bodyMedium = QuizzTypography.bodyMedium,
    bodySmall = QuizzTypography.bodySmall,
    labelLarge = QuizzTypography.labelLarge,
    labelMedium = QuizzTypography.labelMedium,
    labelSmall = QuizzTypography.labelSmall,
)

private val TvShapes = Shapes(
    extraSmall = RoundedCornerShape(QuizzShape.extraSmall.dp),
    small = RoundedCornerShape(QuizzShape.small.dp),
    medium = RoundedCornerShape(QuizzShape.medium.dp),
    large = RoundedCornerShape(QuizzShape.large.dp),
    extraLarge = RoundedCornerShape(QuizzShape.extraLarge.dp),
)

@Composable
fun QuizzTvTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalQuizzGameColors provides TvDarkGameColors) {
        MaterialTheme(colorScheme = TvDarkColors, typography = TvTypography, shapes = TvShapes, content = content)
    }
}
