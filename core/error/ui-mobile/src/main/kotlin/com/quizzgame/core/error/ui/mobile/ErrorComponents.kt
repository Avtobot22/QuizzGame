package com.quizzgame.core.error.ui.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.designsystem.mobile.QuizzInlineMessage
import com.quizzgame.core.designsystem.mobile.QuizzNoticeBanner
import com.quizzgame.core.designsystem.mobile.QuizzStatusContent
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.ui.common.ErrorUiModel
import com.quizzgame.core.error.ui.common.errorUiModel
import com.quizzgame.core.error.ui.common.requireErrorSurface
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions

/**
 * Блокирующее состояние внутри текущей feature. Принимает только BLOCKING; не создаёт маршрут.
 * Shell учитывает безопасные insets и ограниченную высоту, сохраняет доступность прокруткой.
 * onAction передаёт намерение; повтор, вход и Back выполняет владелец. enabled блокирует кнопки
 * на время операции; защиту от параллельных запросов также обеспечивает владелец.
 */
@Composable
fun QuizzMobileErrorFullScreen(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.BLOCKING)
    val model = errorUiModel(spec)
    Box(
        modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = QuizzStatusDimensions.mobileWidth.dp)
                .fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            QuizzStatusContent(
                modifier = Modifier.fillMaxWidth(),
                tone = QuizzNoticeTone.ERROR,
                icon = model.icon,
                actions = { ErrorActions(model, enabled, onAction) },
            ) { ErrorCopy(model, heading = true) }
        }
    }
}

/** Баннер для сохранённого контента. Принимает только BANNER и не перемещает текущий фокус. */
@Composable
fun QuizzMobileErrorBanner(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.BANNER)
    val model = errorUiModel(spec)
    QuizzNoticeBanner(modifier, QuizzNoticeTone.ERROR, model.icon, { ErrorActions(model, enabled, onAction) }) {
        ErrorCopy(model)
    }
}

/** Локальная ошибка формы. Принимает только INLINE; место относительно поля задаёт вызывающий код. */
@Composable
fun QuizzMobileErrorInline(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.INLINE)
    val model = errorUiModel(spec)
    QuizzInlineMessage(modifier, QuizzNoticeTone.ERROR, model.icon, { ErrorActions(model, enabled, onAction) }) {
        ErrorCopy(model)
    }
}

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
