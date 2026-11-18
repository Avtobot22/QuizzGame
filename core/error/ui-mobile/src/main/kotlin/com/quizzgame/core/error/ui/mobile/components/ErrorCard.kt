package com.quizzgame.core.error.ui.mobile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.designsystem.mobile.QuizzStatusSymbol
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.ui.common.presentation.ErrorUiModel
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.core.ui.status.QuizzNoticeTone

/** Локальная ошибка; компактный вариант заполняет размеры карточки, заданные владельцем. */
@Composable
fun QuizzMobileErrorCard(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val model = errorUiModel(spec)
    Surface(
        modifier,
        shape = if (compact) MaterialTheme.shapes.large else MaterialTheme.shapes.extraLarge,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, if (compact) colors.outlineVariant else colors.outlineVariant.copy(alpha = 0.5f)),
    ) {
        BoxWithConstraints(
            Modifier.background(
                Brush.linearGradient(
                    listOf(
                        colors.primaryContainer.copy(alpha = 0.25f),
                        colors.surfaceContainerLow,
                    ),
                ),
            ).padding(if (compact) 12.dp else 20.dp).semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            if (compact) {
                CompactErrorCardContent(model, onAction)
            } else if (maxWidth < 260.dp) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    ErrorCardIllustration(model)
                    ErrorCardDetails(model, onAction, centered = true)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ErrorCardIllustration(model)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErrorCardDetails(model, onAction, centered = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactErrorCardContent(model: ErrorUiModel, onAction: (ErrorActionKind) -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            ErrorCardIllustration(model, size = 48.dp, iconSize = 20.dp)
            ErrorCardCopy(model, centered = true, supportingMaxLines = 3)
        }
        ErrorCardActions(model, onAction, Alignment.CenterHorizontally)
    }
}

@Composable
private fun ErrorCardIllustration(model: ErrorUiModel, size: Dp = 80.dp, iconSize: Dp = 26.dp) {
    val colors = MaterialTheme.colorScheme
    QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(size)) {
        QuizzStatusSymbol(model.icon, Modifier.size(iconSize), QuizzNoticeTone.ERROR)
    }
}

@Composable
private fun ColumnScope.ErrorCardDetails(model: ErrorUiModel, onAction: (ErrorActionKind) -> Unit, centered: Boolean) {
    ErrorCardCopy(model, centered)
    val alignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ErrorCardActions(model, onAction, alignment)
}

@Composable
private fun ErrorCardCopy(model: ErrorUiModel, centered: Boolean, supportingMaxLines: Int = Int.MAX_VALUE) {
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(model.title, style = MaterialTheme.typography.titleMedium, textAlign = textAlign)
        Text(
            errorSupportingMessage(model),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
            maxLines = supportingMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ColumnScope.ErrorCardActions(
    model: ErrorUiModel,
    onAction: (ErrorActionKind) -> Unit,
    alignment: Alignment.Horizontal,
) {
    model.actions.forEach { action ->
        QuizzButton({ onAction(action.kind) }, Modifier.align(alignment), style = QuizzButtonStyle.TONAL) {
            Text(action.label)
        }
    }
}
