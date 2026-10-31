package com.quizzgame.core.error.ui.common

import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Одноразовые уведомления одного экрана. Создайте экземпляр у его владельца и не переиспользуйте
 * между feature. Нет replay и сохранения: без активного host событие теряется. При перегрузке
 * остаётся последнее уведомление; исходная операция и её callbacks здесь не хранятся.
 */
class ErrorNoticeEvents {
    private val mutableEvents = MutableSharedFlow<ErrorPresentationSpec>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    internal val events = mutableEvents.asSharedFlow()

    /** Принимает только TRANSIENT; неправильная поверхность является ошибкой интеграции. */
    fun show(spec: ErrorPresentationSpec) {
        requireErrorSurface(spec, ErrorSurface.TRANSIENT)
        mutableEvents.tryEmit(spec)
    }
}
