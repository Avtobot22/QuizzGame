package com.quizzgame.core.error.ui.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.common.presentation.ErrorUiModel
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun ErrorCopy(model: ErrorUiModel, heading: Boolean = false) {
    Column(
        horizontalAlignment = if (heading) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.gap.dp),
    ) {
        Text(
            model.title,
            modifier = if (heading) Modifier.semantics { heading() } else Modifier,
            style = if (heading) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
            textAlign = if (heading) TextAlign.Center else TextAlign.Start,
        )
        val message = model.message ?: if (heading) errorSupportingMessage(model) else null
        message?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (heading) TextAlign.Center else TextAlign.Start,
            )
        }
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
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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

@Composable
internal fun errorSupportingMessage(model: ErrorUiModel): String = model.message ?: stringResource(
    when (model.icon) {
        QuizzStatusIcon.NETWORK -> UiR.string.quizz_status_network_help
        QuizzStatusIcon.SERVER -> UiR.string.quizz_status_server_help
        QuizzStatusIcon.AUTH -> UiR.string.quizz_status_auth_help
        else -> UiR.string.quizz_status_generic_help
    },
)
