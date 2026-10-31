package com.quizzgame.core.error.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ui.common.ErrorNoticeEvents
import com.quizzgame.core.error.ui.common.errorUiModel
import com.quizzgame.core.error.ui.common.rememberErrorNoticeState
import com.quizzgame.core.ui.status.QuizzNoticeTone

/**
 * Host текущего экрана: latest-wins, без replay, только при lifecycle STARTED.
 * Для другого экрана создайте новый ErrorNoticeEvents. Удаление host и смена владельца очищают notice.
 * TV с действиями сохраняет доступный баннер до выбора или замены, без перемещения фокуса.
 * enabled отключает кнопки на время операции; onAction передаёт намерение владельцу.
 */
@Composable
fun QuizzTvErrorNoticeHost(
    events: ErrorNoticeEvents,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    durationMillis: Long = 5_000,
) {
    require(durationMillis > 0) { "Notice duration must be positive" }
    val state = rememberErrorNoticeState(events) { spec -> if (spec.actions.isNotEmpty()) null else durationMillis }
    val latestAction by rememberUpdatedState(onAction)
    val notice = state.current ?: return
    val model = errorUiModel(notice.spec)
    val handleAction: (ErrorActionKind) -> Unit = { action ->
        if (state.dismiss(notice)) latestAction(action)
    }
    QuizzTvNoticeBanner(
        modifier = modifier,
        tone = QuizzNoticeTone.ERROR,
        icon = model.icon,
        actions = { ErrorActions(model, enabled, handleAction) },
    ) { ErrorCopy(model) }
}
