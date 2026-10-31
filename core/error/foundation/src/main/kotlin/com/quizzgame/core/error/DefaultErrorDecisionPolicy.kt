package com.quizzgame.core.error

/**
 * Правила представления инфраструктурных ошибок без отрисовки и выполнения действий.
 *
 * INITIAL_LOAD и USER_REFRESH выбирают BLOCKING без пригодного контента и BANNER при его наличии.
 * USER_COMMAND выбирает TRANSIENT, AUTH_FLOW - INLINE, сохраняя экран и введённые данные.
 * Необязательные BACKGROUND/PREFETCH выбирают SILENT с пустым списком действий независимо от контента.
 * Критичную фоновую работу и доступность навигации определяет отдельная политика feature.
 *
 * Ошибки соединения, таймаут, недоступность сервера и rate limit допускают ручное восстановление:
 * SAFE_TO_REPEAT даёт RETRY, REQUIRES_CONFIRMATION - только CONFIRM_RETRY, FORBIDDEN - ни одно из них.
 * Подтверждение и повтор выполняет feature. Отказ доступа, отклонённый запрос, неизвестный HTTP,
 * некорректные данные и ошибка хранилища не получают повтор по умолчанию.
 * В debug ошибка конфигурации допускает повтор после её исправления с учётом тех же правил безопасности.
 *
 * Ошибка авторизации предлагает SIGN_IN вместо повтора, но не вызывает вход и не меняет сессию.
 * BANNER/TRANSIENT дополнительно допускают DISMISS; INLINE/BLOCKING содержат только восстановительное
 * действие, если оно доступно. Политика не обещает GO_BACK, EXIT или OPEN_SETTINGS без контекста feature.
 *
 * ```
 * val policy: ErrorDecisionPolicy = DefaultErrorDecisionPolicy()
 * val decision = policy.decide(
 *     AppFailure.Service(ServiceKind.UNAVAILABLE, 503),
 *     ErrorOperationContext(OperationKind.USER_REFRESH, true, RetrySafety.SAFE_TO_REPEAT),
 * ) // BANNER, SERVER_UNAVAILABLE, действия RETRY и DISMISS
 * ```
 */
class DefaultErrorDecisionPolicy : ErrorDecisionPolicy {

    override fun decide(failure: AppFailure, context: ErrorOperationContext): ErrorPresentationSpec {
        val surface = context.surface()
        val presentation = failurePresentation(failure, context.retrySafety)
        return ErrorPresentationSpec(
            surface = surface,
            title = presentation.copyKey,
            actions = when (surface) {
                ErrorSurface.SILENT -> emptyList()
                ErrorSurface.BANNER,
                ErrorSurface.TRANSIENT,
                -> listOfNotNull(presentation.action, ErrorActionKind.DISMISS)

                ErrorSurface.INLINE,
                ErrorSurface.BLOCKING,
                -> listOfNotNull(presentation.action)
            },
        )
    }
}

private fun ErrorOperationContext.surface(): ErrorSurface = when (operation) {
    OperationKind.INITIAL_LOAD,
    OperationKind.USER_REFRESH,
    -> if (hasUsableContent) ErrorSurface.BANNER else ErrorSurface.BLOCKING

    OperationKind.USER_COMMAND -> ErrorSurface.TRANSIENT
    OperationKind.AUTH_FLOW    -> ErrorSurface.INLINE
    OperationKind.BACKGROUND,
    OperationKind.PREFETCH,
    -> ErrorSurface.SILENT
}

internal data class FailurePresentation(val copyKey: ErrorCopyKey, val action: ErrorActionKind? = null)

internal fun RetrySafety.action(): ErrorActionKind? = when (this) {
    RetrySafety.SAFE_TO_REPEAT        -> ErrorActionKind.RETRY
    RetrySafety.REQUIRES_CONFIRMATION -> ErrorActionKind.CONFIRM_RETRY
    RetrySafety.FORBIDDEN             -> null
}

internal fun AppFailure.Service.presentation(safety: RetrySafety): FailurePresentation = when (kind) {
    ServiceKind.UNAVAILABLE   -> FailurePresentation(ErrorCopyKey.SERVER_UNAVAILABLE, safety.action())
    ServiceKind.RATE_LIMITED  -> FailurePresentation(ErrorCopyKey.TOO_MANY_REQUESTS, safety.action())
    ServiceKind.ACCESS_DENIED -> FailurePresentation(ErrorCopyKey.ACCESS_DENIED)
    ServiceKind.CLIENT_REJECTED,
    ServiceKind.OTHER_HTTP,
    -> FailurePresentation(ErrorCopyKey.GENERIC_FAILURE)
}

internal fun AppFailure.Connectivity.presentation(safety: RetrySafety): FailurePresentation = FailurePresentation(
    copyKey = when (kind) {
        ConnectivityKind.OFFLINE_OR_UNREACHABLE -> ErrorCopyKey.CONNECTION_PROBLEM
        ConnectivityKind.TIMEOUT                -> ErrorCopyKey.REQUEST_TIMED_OUT
    },
    action = safety.action(),
)

internal fun AppFailure.Authentication.presentation(): FailurePresentation = FailurePresentation(
    copyKey = when (kind) {
        AuthenticationKind.REQUIRED -> ErrorCopyKey.SIGN_IN_REQUIRED
        AuthenticationKind.REVOKED  -> ErrorCopyKey.SESSION_EXPIRED
    },
    action = ErrorActionKind.SIGN_IN,
)
