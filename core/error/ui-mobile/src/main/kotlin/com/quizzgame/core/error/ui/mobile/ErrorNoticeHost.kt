package com.quizzgame.core.error.ui.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.quizzgame.core.designsystem.mobile.QuizzStatusSymbol
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ui.common.ErrorNoticeEvents
import com.quizzgame.core.error.ui.common.errorUiModel
import com.quizzgame.core.error.ui.common.rememberErrorNoticeState

/**
 * Host текущего экрана: latest-wins, без replay, только при lifecycle STARTED.
 * Для другого экрана создайте новый ErrorNoticeEvents. Удаление host и смена владельца очищают notice.
 * Snackbar исчезает с учётом accessibility timeout или после выбора действия.
 * enabled отключает кнопки на время операции; onAction передаёт намерение владельцу.
 */
@Composable
fun QuizzMobileErrorNoticeHost(
    events: ErrorNoticeEvents,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    durationMillis: Long = 5_000,
) {
    require(durationMillis > 0) { "Notice duration must be positive" }
    val state = rememberErrorNoticeState(events) { _ -> durationMillis }
    val latestAction by rememberUpdatedState(onAction)
    val notice = state.current ?: return
    val model = errorUiModel(notice.spec)
    val handleAction: (ErrorActionKind) -> Unit = { action ->
        if (state.dismiss(notice)) latestAction(action)
    }
    Snackbar(
        modifier = modifier,
        actionOnNewLine = true,
        action = { ErrorActions(model, enabled, handleAction) },
    ) {
        Column {
            QuizzStatusSymbol(model.icon)
            ErrorCopy(model)
        }
    }
}
