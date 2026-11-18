package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quizzgame.core.ui.status.QuizzStatusIcon

/** Информационный диалог с явными полями от экрана, в том числе внутри Compose Preview. */
@Composable
fun QuizzInfoDialog(
    title: String,
    message: String,
    closeLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: QuizzStatusIcon = QuizzStatusIcon.ROOM,
) {
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.padding(
                horizontal = 28.dp,
            ).widthIn(max = QuizzMobileLayout.dialogWidth).fillMaxWidth().then(modifier),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
                    QuizzStatusSymbol(icon, Modifier.padding(12.dp).size(24.dp))
                }
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                QuizzButton(onDismiss, Modifier.align(Alignment.End), style = QuizzButtonStyle.TONAL) {
                    Text(closeLabel)
                }
            }
        }
    }
}
