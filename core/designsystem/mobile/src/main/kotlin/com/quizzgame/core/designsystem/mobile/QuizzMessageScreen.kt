package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.status.QuizzStatusIcon

@Composable
fun QuizzMessageScreen(
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
        QuizzStatusContent(
            Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
            icon = icon,
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
