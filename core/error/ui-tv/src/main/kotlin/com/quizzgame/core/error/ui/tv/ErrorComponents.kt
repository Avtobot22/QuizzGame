package com.quizzgame.core.error.ui.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.quizzgame.core.designsystem.tv.QuizzTvInlineMessage
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.designsystem.tv.QuizzTvStatusContent
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.ui.common.ErrorUiModel
import com.quizzgame.core.error.ui.common.errorUiModel
import com.quizzgame.core.error.ui.common.requireErrorSurface
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.tokens.QuizzDimensions

/**
 * Блокирующее состояние внутри текущей feature. Принимает только BLOCKING; не создаёт маршрут.
 * Shell учитывает безопасные insets и ограниченную высоту, сохраняет доступность прокруткой.
 * onAction передаёт намерение; повтор, вход и Back выполняет владелец. enabled блокирует кнопки
 * на время операции; защиту от параллельных запросов также обеспечивает владелец.
 * requestInitialFocus отключает первоначальный запрос фокуса на первую доступную кнопку.
 */
@Composable
fun QuizzTvErrorFullScreen(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    requestInitialFocus: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.BLOCKING)
    val model = errorUiModel(spec)
    val firstAction = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(
                horizontal = QuizzDimensions.tvSafeHorizontalDp.dp,
                vertical = QuizzDimensions.tvSafeVerticalDp.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = QuizzStatusDimensions.tvWidth.dp)
                .fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            QuizzTvStatusContent(
                modifier = Modifier.fillMaxWidth(),
                tone = QuizzNoticeTone.ERROR,
                icon = model.icon,
                actions = { ErrorActions(model, enabled, onAction, firstAction) },
            ) { ErrorCopy(model, heading = true) }
        }
    }
    val canRequestFocus = enabled && requestInitialFocus && model.actions.isNotEmpty()
    LaunchedEffect(canRequestFocus) {
        if (canRequestFocus && !focused) {
            focused = firstAction.requestFocus()
        }
    }
}

/** Баннер для сохранённого контента. Принимает только BANNER и не перемещает текущий фокус. */
@Composable
fun QuizzTvErrorBanner(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.BANNER)
    val model = errorUiModel(spec)
    QuizzTvNoticeBanner(modifier, QuizzNoticeTone.ERROR, model.icon, { ErrorActions(model, enabled, onAction) }) {
        ErrorCopy(model)
    }
}

/** Локальная ошибка формы. Принимает только INLINE; место относительно поля задаёт вызывающий код. */
@Composable
fun QuizzTvErrorInline(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.INLINE)
    val model = errorUiModel(spec)
    QuizzTvInlineMessage(modifier, QuizzNoticeTone.ERROR, model.icon, { ErrorActions(model, enabled, onAction) }) {
        ErrorCopy(model)
    }
}

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
