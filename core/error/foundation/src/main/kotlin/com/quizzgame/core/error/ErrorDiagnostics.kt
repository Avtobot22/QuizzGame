package com.quizzgame.core.error

/**
 * Приёмник готового безопасного конверта. Реализация и способ хранения принадлежат вызывающему коду.
 *
 * По умолчанию используй [NoOpErrorDiagnostics]. Подключение приёмника не должно менять классификацию,
 * состояние сессии или решение о показе ошибки. Этот модуль не выбирает логгер или провайдер телеметрии.
 *
 * ```
 * enum class Operations { LOAD, REFRESH }
 * val diagnostics: ErrorDiagnostics = NoOpErrorDiagnostics
 * diagnostics.record(
 *     ErrorDiagnostic.from(
 *         failure = AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
 *         operationKey = ErrorOperationKey.from(Operations.LOAD),
 *     ),
 * )
 * ```
 *
 * Для корреляции вызывающий код может передать UUID в [ErrorDiagnostic.from] заранее.
 * [record] не возвращает ID и не определяет представление ошибки.
 */
fun interface ErrorDiagnostics {
    fun record(diagnostic: ErrorDiagnostic)
}
