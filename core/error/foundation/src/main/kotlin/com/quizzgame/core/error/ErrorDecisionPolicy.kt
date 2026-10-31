package com.quizzgame.core.error

/**
 * Правило выбора представления ошибки по её смыслу и контексту операции.
 *
 * Один и тот же вход должен давать одинаковое решение без чтения ресурсов, времени или состояния
 * сессии. [decide] не выполняет I/O, повтор, вход или навигацию. Результат описывает возможности:
 * владелец feature проверяет их доступность и обрабатывает выбранное пользователем действие.
 */
fun interface ErrorDecisionPolicy {
    fun decide(failure: AppFailure, context: ErrorOperationContext): ErrorPresentationSpec
}
