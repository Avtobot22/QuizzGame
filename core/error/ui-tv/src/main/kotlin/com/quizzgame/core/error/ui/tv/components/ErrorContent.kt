package com.quizzgame.core.error.ui.tv.components

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvStatusCopy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.common.presentation.ErrorUiModel
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun ErrorCopy(model: ErrorUiModel, heading: Boolean = false) {
    val backOnly = model.actions.singleOrNull()?.kind == ErrorActionKind.GO_BACK
    val message = model.message ?: if (heading && !backOnly) {
        stringResource(
            when (model.icon) {
                QuizzStatusIcon.NETWORK -> UiR.string.quizz_status_network_help
                QuizzStatusIcon.SERVER -> UiR.string.quizz_status_server_help
                QuizzStatusIcon.AUTH -> UiR.string.quizz_status_auth_help
                else -> UiR.string.quizz_status_generic_help
            },
        )
    } else {
        null
    }
    if (heading) {
        QuizzTvStatusCopy(model.title, message)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.gap.dp)) {
            Text(model.title, style = MaterialTheme.typography.titleMedium)
            message?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun ErrorActions(
    model: ErrorUiModel,
    enabled: Boolean,
    onAction: (ErrorActionKind) -> Unit,
    firstAction: FocusRequester? = null,
) {
    if (model.actions.isEmpty()) return
    FlowRow(
        modifier = Modifier.focusRestorer().focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        model.actions.forEachIndexed { index, action ->
            QuizzTvButton(
                onClick = { onAction(action.kind) },
                enabled = enabled,
                style = when (action.kind) {
                    ErrorActionKind.RETRY, ErrorActionKind.CONFIRM_RETRY,
                    ErrorActionKind.SIGN_IN, ErrorActionKind.OPEN_SETTINGS,
                    -> QuizzTvButtonStyle.PRIMARY
                    ErrorActionKind.GO_BACK, ErrorActionKind.DISMISS,
                    ErrorActionKind.EXIT,
                    -> QuizzTvButtonStyle.SECONDARY
                },
                modifier = if (index == 0 && firstAction != null) Modifier.focusRequester(firstAction) else Modifier,
            ) { QuizzTvActionLabel(action.label) }
        }
    }
}
