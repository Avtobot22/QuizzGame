package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme

@Composable
fun QuizzTvActionLabel(text: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.labelLarge.copy(color = LocalContentColor.current)
    BasicText(
        text,
        modifier = modifier,
        style = style,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = style.fontSize, stepSize = 1.sp),
    )
}
