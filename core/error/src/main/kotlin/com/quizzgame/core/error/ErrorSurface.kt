package com.quizzgame.core.error

/**
 * Место показа внутри feature: INLINE в форме, BANNER поверх сохранённого контента,
 * TRANSIENT как краткое уведомление, BLOCKING как состояние без пригодного контента,
 * SILENT без уведомления. BLOCKING не требует отдельного экрана или перехода.
 */
enum class ErrorSurface {
    INLINE,
    BANNER,
    TRANSIENT,
    BLOCKING,
    SILENT,
}
