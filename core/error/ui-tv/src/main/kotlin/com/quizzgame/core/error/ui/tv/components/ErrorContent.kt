package com.quizzgame.core.error.ui.tv.components

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.common.presentation.ErrorUiModel
import com.quizzgame.core.ui.status.QuizzStatusDimensions

@Composable
internal fun ErrorCopy(model: ErrorUiModel, heading: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.gap.dp)) {
        Text(
            model.title,
            modifier = if (heading) Modifier.semantics { heading() } else Modifier,
            style = MaterialTheme.typography.headlineLarge,
        )
        model.message?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
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
        horizontalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.actionGap.dp),
        verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.actionGap.dp),
    ) {
        model.actions.forEachIndexed { index, action ->
            QuizzTvButton(
                onClick = { onAction(action.kind) },
                enabled = enabled,
                modifier = if (index == 0 && firstAction != null) Modifier.focusRequester(firstAction) else Modifier,
            ) { Text(action.label) }
        }
    }
}
