package com.quizzgame.core.designsystem.tv

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quizzgame.core.ui.tokens.QuizzFontFamily

internal object TvComponentType {
    val button = TextStyle(
        fontFamily = QuizzFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 28.sp,
    )
    val answerTitle = TextStyle(
        fontFamily = QuizzFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 31.sp,
        lineHeight = 31.sp,
    )
    val answerHelper = TextStyle(fontFamily = QuizzFontFamily, fontSize = 22.sp, lineHeight = 22.sp)
    val panelTitle = TextStyle(
        fontFamily = QuizzFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 32.sp,
    )
    val panelCode = TextStyle(
        fontFamily = QuizzFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 35.sp,
        lineHeight = 35.sp,
    )
    val panelHelper = TextStyle(fontFamily = QuizzFontFamily, fontSize = 21.sp, lineHeight = 21.sp)
}
