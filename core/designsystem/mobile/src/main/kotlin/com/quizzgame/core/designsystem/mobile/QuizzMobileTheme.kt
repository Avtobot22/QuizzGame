package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.LocalQuizzGameColors
import com.quizzgame.core.ui.QuizzGameColors
import com.quizzgame.core.ui.tokens.QuizzShape
import com.quizzgame.core.ui.tokens.QuizzTypography

private val MobileLightColors = lightColorScheme(
    primary = MobileLightPalette.primary,
    onPrimary = MobileLightPalette.onPrimary,
    primaryContainer = MobileLightPalette.primaryContainer,
    onPrimaryContainer = MobileLightPalette.onPrimaryContainer,
    primaryFixed = MobileLightPalette.primaryFixed,
    primaryFixedDim = MobileLightPalette.primaryFixedDim,
    onPrimaryFixed = MobileLightPalette.onPrimaryFixed,
    onPrimaryFixedVariant = MobileLightPalette.onPrimaryFixedVariant,
    secondary = MobileLightPalette.secondary,
    onSecondary = MobileLightPalette.onSecondary,
    secondaryContainer = MobileLightPalette.secondaryContainer,
    onSecondaryContainer = MobileLightPalette.onSecondaryContainer,
    secondaryFixed = MobileLightPalette.secondaryFixed,
    secondaryFixedDim = MobileLightPalette.secondaryFixedDim,
    onSecondaryFixed = MobileLightPalette.onSecondaryFixed,
    onSecondaryFixedVariant = MobileLightPalette.onSecondaryFixedVariant,
    tertiary = MobileLightPalette.tertiary,
    onTertiary = MobileLightPalette.onTertiary,
    tertiaryContainer = MobileLightPalette.tertiaryContainer,
    onTertiaryContainer = MobileLightPalette.onTertiaryContainer,
    tertiaryFixed = MobileLightPalette.tertiaryFixed,
    tertiaryFixedDim = MobileLightPalette.tertiaryFixedDim,
    onTertiaryFixed = MobileLightPalette.onTertiaryFixed,
    onTertiaryFixedVariant = MobileLightPalette.onTertiaryFixedVariant,
    error = MobileLightPalette.error,
    onError = MobileLightPalette.onError,
    errorContainer = MobileLightPalette.errorContainer,
    onErrorContainer = MobileLightPalette.onErrorContainer,
    background = MobileLightPalette.background,
    onBackground = MobileLightPalette.onBackground,
    surface = MobileLightPalette.surface,
    onSurface = MobileLightPalette.onSurface,
    surfaceVariant = MobileLightPalette.surfaceVariant,
    onSurfaceVariant = MobileLightPalette.onSurfaceVariant,
    surfaceDim = MobileLightPalette.surfaceDim,
    surfaceBright = MobileLightPalette.surfaceBright,
    surfaceContainerLowest = MobileLightPalette.surfaceContainerLowest,
    surfaceContainerLow = MobileLightPalette.surfaceContainerLow,
    surfaceContainer = MobileLightPalette.surfaceContainer,
    surfaceContainerHigh = MobileLightPalette.surfaceContainerHigh,
    surfaceContainerHighest = MobileLightPalette.surfaceContainerHighest,
    inverseSurface = MobileLightPalette.inverseSurface,
    inverseOnSurface = MobileLightPalette.inverseOnSurface,
    inversePrimary = MobileLightPalette.inversePrimary,
    outline = MobileLightPalette.outline,
    outlineVariant = MobileLightPalette.outlineVariant,
    surfaceTint = MobileLightPalette.surfaceTint,
    scrim = MobileLightPalette.scrim,
)

private val MobileLightGameColors = QuizzGameColors(
    answerCorrect = GameMobileLightPalette.answerCorrect,
    onAnswerCorrect = GameMobileLightPalette.onAnswerCorrect,
    answerWrong = GameMobileLightPalette.answerWrong,
    onAnswerWrong = GameMobileLightPalette.onAnswerWrong,
    answerSelected = GameMobileLightPalette.answerSelected,
    onAnswerSelected = GameMobileLightPalette.onAnswerSelected,
    timerAccent = GameMobileLightPalette.timerAccent,
    onTimerAccent = GameMobileLightPalette.onTimerAccent,
    streak = GameMobileLightPalette.streak,
    onStreak = GameMobileLightPalette.onStreak,
    scoreHighlight = GameMobileLightPalette.scoreHighlight,
    onScoreHighlight = GameMobileLightPalette.onScoreHighlight,
    answerIdle = GameMobileLightPalette.answerIdle,
    onAnswerIdle = GameMobileLightPalette.onAnswerIdle,
)

private val MobileDarkColors = darkColorScheme(
    primary = MobileDarkPalette.primary,
    onPrimary = MobileDarkPalette.onPrimary,
    primaryContainer = MobileDarkPalette.primaryContainer,
    onPrimaryContainer = MobileDarkPalette.onPrimaryContainer,
    primaryFixed = MobileDarkPalette.primaryFixed,
    primaryFixedDim = MobileDarkPalette.primaryFixedDim,
    onPrimaryFixed = MobileDarkPalette.onPrimaryFixed,
    onPrimaryFixedVariant = MobileDarkPalette.onPrimaryFixedVariant,
    secondary = MobileDarkPalette.secondary,
    onSecondary = MobileDarkPalette.onSecondary,
    secondaryContainer = MobileDarkPalette.secondaryContainer,
    onSecondaryContainer = MobileDarkPalette.onSecondaryContainer,
    secondaryFixed = MobileDarkPalette.secondaryFixed,
    secondaryFixedDim = MobileDarkPalette.secondaryFixedDim,
    onSecondaryFixed = MobileDarkPalette.onSecondaryFixed,
    onSecondaryFixedVariant = MobileDarkPalette.onSecondaryFixedVariant,
    tertiary = MobileDarkPalette.tertiary,
    onTertiary = MobileDarkPalette.onTertiary,
    tertiaryContainer = MobileDarkPalette.tertiaryContainer,
    onTertiaryContainer = MobileDarkPalette.onTertiaryContainer,
    tertiaryFixed = MobileDarkPalette.tertiaryFixed,
    tertiaryFixedDim = MobileDarkPalette.tertiaryFixedDim,
    onTertiaryFixed = MobileDarkPalette.onTertiaryFixed,
    onTertiaryFixedVariant = MobileDarkPalette.onTertiaryFixedVariant,
    error = MobileDarkPalette.error,
    onError = MobileDarkPalette.onError,
    errorContainer = MobileDarkPalette.errorContainer,
    onErrorContainer = MobileDarkPalette.onErrorContainer,
    background = MobileDarkPalette.background,
    onBackground = MobileDarkPalette.onBackground,
    surface = MobileDarkPalette.surface,
    onSurface = MobileDarkPalette.onSurface,
    surfaceVariant = MobileDarkPalette.surfaceVariant,
    onSurfaceVariant = MobileDarkPalette.onSurfaceVariant,
    surfaceDim = MobileDarkPalette.surfaceDim,
    surfaceBright = MobileDarkPalette.surfaceBright,
    surfaceContainerLowest = MobileDarkPalette.surfaceContainerLowest,
    surfaceContainerLow = MobileDarkPalette.surfaceContainerLow,
    surfaceContainer = MobileDarkPalette.surfaceContainer,
    surfaceContainerHigh = MobileDarkPalette.surfaceContainerHigh,
    surfaceContainerHighest = MobileDarkPalette.surfaceContainerHighest,
    inverseSurface = MobileDarkPalette.inverseSurface,
    inverseOnSurface = MobileDarkPalette.inverseOnSurface,
    inversePrimary = MobileDarkPalette.inversePrimary,
    outline = MobileDarkPalette.outline,
    outlineVariant = MobileDarkPalette.outlineVariant,
    surfaceTint = MobileDarkPalette.surfaceTint,
    scrim = MobileDarkPalette.scrim,
)

private val MobileDarkGameColors = QuizzGameColors(
    answerCorrect = GameMobileDarkPalette.answerCorrect,
    onAnswerCorrect = GameMobileDarkPalette.onAnswerCorrect,
    answerWrong = GameMobileDarkPalette.answerWrong,
    onAnswerWrong = GameMobileDarkPalette.onAnswerWrong,
    answerSelected = GameMobileDarkPalette.answerSelected,
    onAnswerSelected = GameMobileDarkPalette.onAnswerSelected,
    timerAccent = GameMobileDarkPalette.timerAccent,
    onTimerAccent = GameMobileDarkPalette.onTimerAccent,
    streak = GameMobileDarkPalette.streak,
    onStreak = GameMobileDarkPalette.onStreak,
    scoreHighlight = GameMobileDarkPalette.scoreHighlight,
    onScoreHighlight = GameMobileDarkPalette.onScoreHighlight,
    answerIdle = GameMobileDarkPalette.answerIdle,
    onAnswerIdle = GameMobileDarkPalette.onAnswerIdle,
)

private val MobileTypography = Typography(
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

private val MobileShapes = Shapes(
    extraSmall = RoundedCornerShape(QuizzShape.extraSmall.dp),
    small = RoundedCornerShape(QuizzShape.small.dp),
    medium = RoundedCornerShape(QuizzShape.medium.dp),
    large = RoundedCornerShape(QuizzShape.large.dp),
    extraLarge = RoundedCornerShape(QuizzShape.extraLarge.dp),
)

@Composable
fun QuizzMobileTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalQuizzGameColors provides if (darkTheme) MobileDarkGameColors else MobileLightGameColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) MobileDarkColors else MobileLightColors,
            typography = MobileTypography,
            shapes = MobileShapes,
            content = content,
        )
    }
}
