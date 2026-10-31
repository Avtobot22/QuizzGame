package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.status.QuizzStatusDimensions

/**
 * Подтверждение произвольного действия. Confirm или Cancel доставляются один раз за показ.
 * Back и закрытие снаружи означают Cancel; операция и удаление диалога остаются владельцу.
 * На TV начальный фокус принадлежит безопасной кнопке Cancel.
 * [returnFocusRequester] возвращает фокус после закрытия, если цель ещё находится на экране.
 */
@Composable
fun QuizzConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(R.string.quizz_confirmation_confirm),
    cancelLabel: String = stringResource(R.string.quizz_confirmation_cancel),
    enabled: Boolean = true,
    returnFocusRequester: FocusRequester? = null,
) {
    var completed by remember { mutableStateOf(false) }
    val dismiss = {
        if (!completed) {
            completed = true
            onDismissRequest()
        }
    }
    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true),
    ) {
        val colors = MaterialTheme.colorScheme
        Surface(
            modifier = modifier.widthIn(max = QuizzStatusDimensions.mobileWidth.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.surface,
            contentColor = colors.onSurface,
        ) {
            Column(
                modifier = Modifier.padding(QuizzStatusDimensions.padding.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.gap.dp),
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(text, style = MaterialTheme.typography.bodyLarge)
                ConfirmationActions(cancelLabel, confirmLabel, enabled && !completed, dismiss) {
                    if (!completed) {
                        completed = true
                        onConfirm()
                    }
                }
            }
        }
    }
    DisposableEffect(returnFocusRequester) {
        onDispose {
            try {
                returnFocusRequester?.requestFocus()
            } catch (_: IllegalStateException) {
                // Владелец мог удалить цель при уходе со страницы вместе с диалогом.
            }
        }
    }
}

@Composable
private fun ConfirmationActions(
    cancelLabel: String,
    confirmLabel: String,
    enabled: Boolean,
    dismiss: () -> Unit,
    confirm: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.actionGap.dp),
        verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.actionGap.dp),
    ) {
        QuizzButton(onClick = dismiss, modifier = Modifier) { Text(cancelLabel) }
        QuizzButton(onClick = confirm, enabled = enabled) { Text(confirmLabel) }
    }
}
