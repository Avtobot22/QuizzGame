package com.quizzgame.core.error

/**
 * Контекст от владельца операции. Его нельзя вывести из HTTP-статуса или технической причины.
 *
 * [hasUsableContent] означает, что текущие данные можно продолжать показывать и использовать.
 * Необязательное изображение относится к PREFETCH, даже если само ещё не загружено:
 * ```
 * ErrorOperationContext(OperationKind.PREFETCH, false, RetrySafety.SAFE_TO_REPEAT)
 * ```
 */
data class ErrorOperationContext(
    val operation: OperationKind,
    val hasUsableContent: Boolean,
    val retrySafety: RetrySafety,
)
