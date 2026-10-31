package com.quizzgame.core.error

/**
 * Решение без привязки к UI-платформе. Адаптер Mobile/TV разрешает локальные ключи текста,
 * а feature обрабатывает действия. [message] заполняется, если нужен отдельный поясняющий текст.
 *
 * SILENT не содержит действий. Действия уникальны: RETRY несовместим с CONFIRM_RETRY,
 * а SIGN_IN не совмещается с повтором исходной операции. Backend-тексты, данные запросов,
 * обработчики и состояние навигации в модель не входят.
 */
data class ErrorPresentationSpec(
    val surface: ErrorSurface,
    val title: ErrorCopyKey,
    val message: ErrorCopyKey? = null,
    val actions: List<ErrorActionKind> = emptyList(),
) {
    init {
        require(surface != ErrorSurface.SILENT || actions.isEmpty()) { "Silent errors cannot offer actions" }
        require(actions.distinct().size == actions.size) { "Error actions must be unique" }
        require(ErrorActionKind.RETRY !in actions || ErrorActionKind.CONFIRM_RETRY !in actions) {
            "Retry must have a single confirmation contract"
        }
        require(ErrorActionKind.SIGN_IN !in actions || actions.none { it.isRetry() }) {
            "Sign-in and retry require different recovery decisions"
        }
    }
}

private fun ErrorActionKind.isRetry(): Boolean =
    this == ErrorActionKind.RETRY || this == ErrorActionKind.CONFIRM_RETRY
