package com.quizzgame.core.ui.tokens

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object QuizzTvTypography {
    val displayLarge = QuizzTypography.displayLarge.copy(fontSize = 48.sp, lineHeight = 56.sp)
    val displayMedium = QuizzTypography.displayMedium.copy(fontSize = 40.sp, lineHeight = 48.sp)
    val displaySmall = QuizzTypography.displaySmall.copy(fontSize = 34.sp, lineHeight = 42.sp)
    val headlineLarge = QuizzTypography.headlineLarge.copy(fontWeight = FontWeight.Bold)
    val headlineMedium = QuizzTypography.headlineMedium.copy(fontSize = 26.sp, lineHeight = 34.sp)
    val headlineSmall = QuizzTypography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 30.sp)
    val titleLarge = QuizzTypography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium)
    val titleMedium = QuizzTypography.titleMedium.copy(fontSize = 18.sp, lineHeight = 24.sp)
    val titleSmall = QuizzTypography.titleSmall.copy(fontSize = 16.sp, lineHeight = 22.sp)
    val bodyLarge = QuizzTypography.bodyLarge.copy(letterSpacing = 0.sp)
    val bodyMedium = QuizzTypography.bodyMedium.copy(letterSpacing = 0.sp)
    val bodySmall = QuizzTypography.bodySmall.copy(letterSpacing = 0.sp)
    val labelLarge = QuizzTypography.labelLarge.copy(fontSize = 16.sp, lineHeight = 22.sp)
    val labelMedium = QuizzTypography.labelMedium.copy(fontSize = 14.sp, lineHeight = 20.sp)
    val labelSmall = QuizzTypography.labelSmall.copy(fontSize = 12.sp, lineHeight = 16.sp)
}
