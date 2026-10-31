package com.quizzgame.core.error

/**
 * Смысл операции для выбора места показа ошибки.
 *
 * INITIAL_LOAD и USER_REFRESH зависят от наличия пригодного контента; USER_COMMAND сохраняет
 * текущий экран и ввод, AUTH_FLOW оставляет ошибку внутри формы входа.
 * BACKGROUND и PREFETCH обозначают необязательную работу, которую можно завершить без уведомления.
 * Критичную фоновую работу обрабатывает отдельная политика feature.
 */
enum class OperationKind {
    INITIAL_LOAD,
    USER_REFRESH,
    USER_COMMAND,
    BACKGROUND,
    PREFETCH,
    AUTH_FLOW,
}
