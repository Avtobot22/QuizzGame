package com.quizzgame.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Показывает инициал рядом с полным именем или общий символ профиля. */
@Composable
fun QuizzProfileAvatar(
    displayName: String?,
    background: Color,
    foreground: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val name = displayName?.trim().orEmpty()
    val initial = name.takeIf(String::isNotEmpty)?.let { it.substring(0, it.offsetByCodePoints(0, 1)).uppercase() }
    Box(
        modifier.background(background, CircleShape).clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        if (initial == null) {
            Image(
                painterResource(R.drawable.ic_quizz_profile),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(foreground),
            )
        } else {
            BasicText(
                initial,
                style = textStyle.copy(color = foreground),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = textStyle.fontSize),
            )
        }
    }
}
