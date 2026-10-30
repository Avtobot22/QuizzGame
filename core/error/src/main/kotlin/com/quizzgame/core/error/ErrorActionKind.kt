package com.quizzgame.core.error

/**
 * Допустимые действия для обработчика feature. Значение не выполняет операцию и не меняет навигацию.
 *
 * CONFIRM_RETRY требует явного согласия пользователя до нового вызова; SIGN_IN лишь предлагает вход,
 * не отзывает сессию и не открывает экран авторизации. DISMISS закрывает уведомление.
 * GO_BACK, EXIT и OPEN_SETTINGS требуют знаний feature о доступных переходах, поэтому политика
 * по умолчанию их не предлагает.
 */
enum class ErrorActionKind {
    RETRY,
    CONFIRM_RETRY,
    GO_BACK,
    SIGN_IN,
    OPEN_SETTINGS,
    DISMISS,
    EXIT,
}
