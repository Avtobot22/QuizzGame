package com.quizzgame.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class QuizzGameColors(
    val answerCorrect: Color,
    val onAnswerCorrect: Color,
    val answerWrong: Color,
    val onAnswerWrong: Color,
    val answerSelected: Color,
    val onAnswerSelected: Color,
    val timerAccent: Color,
    val onTimerAccent: Color,
    val streak: Color,
    val onStreak: Color,
    val scoreHighlight: Color,
    val onScoreHighlight: Color,
    val answerIdle: Color,
    val onAnswerIdle: Color,
)

val LocalQuizzGameColors = staticCompositionLocalOf<QuizzGameColors> {
    error("Wrap content in QuizzMobileTheme or QuizzTvTheme")
}
