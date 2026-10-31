package com.quizzgame.core.error.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.quizzgame.core.error.ErrorPresentationSpec
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/** Одна доставка уведомления. Её identity отличает повторные события с одинаковой спецификацией. */
class ErrorNotice internal constructor(val spec: ErrorPresentationSpec)

/** Инфраструктурное состояние host. Платформенный адаптер рисует current и передаёт выбранное действие. */
class ErrorNoticeState internal constructor() {
    var current: ErrorNotice? by mutableStateOf(null)
        private set

    internal fun replace(spec: ErrorPresentationSpec): ErrorNotice = ErrorNotice(spec).also { current = it }

    /** Закрывает только выбранную доставку. false предотвращает повторную отправку её callback. */
    fun dismiss(notice: ErrorNotice): Boolean {
        if (current !== notice) return false
        current = null
        return true
    }

    internal fun clear() {
        current = null
    }
}

/**
 * Общая lifecycle-aware доставка для платформенных host. Работает только при STARTED.
 * timeoutMillis вычисляется один раз на событие: null сохраняет интерактивное уведомление,
 * положительное значение ограничивает время с учётом настроек доступности.
 *
 * Рекомпозиция не перезапускает сбор: актуальная функция timeout читается через rememberUpdatedState.
 * Остановка lifecycle, смена events/владельца или удаление host очищают сообщение. События не replay-ятся.
 */
@Composable
fun rememberErrorNoticeState(
    events: ErrorNoticeEvents,
    timeoutMillis: (ErrorPresentationSpec) -> Long?,
): ErrorNoticeState {
    val owner = LocalLifecycleOwner.current
    val accessibility = LocalAccessibilityManager.current
    val latestTimeout by rememberUpdatedState(timeoutMillis)
    val state = remember(events, owner) { ErrorNoticeState() }
    LaunchedEffect(events, owner, accessibility) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                events.events.collectLatest { spec ->
                    val notice = state.replace(spec)
                    val requestedTimeout = latestTimeout(spec)
                    if (requestedTimeout == null) awaitCancellation()
                    require(requestedTimeout > 0) { "Notice duration must be positive" }
                    val timeout = accessibility?.calculateRecommendedTimeoutMillis(
                        originalTimeoutMillis = requestedTimeout,
                        containsIcons = true,
                        containsText = true,
                        containsControls = spec.actions.isNotEmpty(),
                    ) ?: requestedTimeout
                    delay(timeout)
                    state.dismiss(notice)
                }
            } finally {
                state.clear()
            }
        }
    }
    DisposableEffect(state) { onDispose { state.clear() } }
    return state
}
