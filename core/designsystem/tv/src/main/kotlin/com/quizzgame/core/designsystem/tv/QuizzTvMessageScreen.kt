package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.ui.status.QuizzStatusIcon

@Composable
fun QuizzTvMessageScreen(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: QuizzStatusIcon = QuizzStatusIcon.GENERIC,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        QuizzTvStatusContent(
            Modifier.widthIn(max = 600.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
            icon = icon,
        ) {
            QuizzTvStatusCopy(title, message)
        }
    }
}
