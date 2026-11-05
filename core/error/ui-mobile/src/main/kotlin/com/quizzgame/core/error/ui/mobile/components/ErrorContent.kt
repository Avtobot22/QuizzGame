package com.quizzgame.core.error.ui.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.common.presentation.ErrorUiModel
import com.quizzgame.core.ui.status.QuizzStatusDimensions

@Composable
internal fun ErrorCopy(model: ErrorUiModel, heading: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.gap.dp)) {
        Text(
            model.title,
            modifier = if (heading) Modifier.semantics { heading() } else Modifier,
            style = MaterialTheme.typography.titleMedium,
        )
        model.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
internal fun ErrorActions(
    model: ErrorUiModel,
    enabled: Boolean,
    onAction: (ErrorActionKind) -> Unit,
) {
    if (model.actions.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.actionGap.dp),
        verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.actionGap.dp),
    ) {
        model.actions.forEach { action ->
            QuizzButton(
                onClick = { onAction(action.kind) },
                enabled = enabled,
                style = if (action.kind == ErrorActionKind.DISMISS || action.kind == ErrorActionKind.GO_BACK) {
                    QuizzButtonStyle.OUTLINED
                } else {
                    QuizzButtonStyle.FILLED
                },
            ) { Text(action.label) }
        }
    }
}
