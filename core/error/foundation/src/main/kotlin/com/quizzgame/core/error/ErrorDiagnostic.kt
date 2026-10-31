package com.quizzgame.core.error

import java.util.UUID

/**
 * Диагностический конверт с ограниченным набором безопасных полей.
 *
 * [operationKey] задаётся статическим enum вызывающей feature; [category] выбирается из закрытого набора.
 * [status] и [reasonCode] сохраняют только числовые данные классифицированной ошибки, включая неизвестные коды.
 * [diagnosticId] необязателен: вызывающий код создаёт UUID вне чистой политики и передаёт готовое значение.
 * UUID не используется как пользовательский текст и не влияет на решение о представлении ошибки.
 *
 * Конверт не хранит исходную ошибку, исключение, состояние сессии, заголовки, URL, тело или backend-тексты.
 * Чтобы получить его из типизированной ошибки, используй [from].
 */
data class ErrorDiagnostic(
    val operationKey: ErrorOperationKey,
    val category: ErrorDiagnosticCategory,
    val status: Int? = null,
    val reasonCode: Int? = null,
    val diagnosticId: UUID? = null,
) {
    companion object {
        /**
         * Чисто проецирует [AppFailure] в разрешённые поля, без `toString()`, I/O и генерации ID.
         * Неизвестный backend reasonCode сохраняется числом и не подменяет семантическую категорию.
         */
        fun from(
            failure: AppFailure,
            operationKey: ErrorOperationKey,
            diagnosticId: UUID? = null,
        ): ErrorDiagnostic {
            val fields = failure.diagnosticFields()
            return ErrorDiagnostic(operationKey, fields.category, fields.status, fields.reasonCode, diagnosticId)
        }
    }
}

internal data class DiagnosticFields(
    val category: ErrorDiagnosticCategory,
    val status: Int? = null,
    val reasonCode: Int? = null,
)

internal fun AppFailure.Connectivity.diagnosticFields(): DiagnosticFields = DiagnosticFields(
    when (kind) {
        ConnectivityKind.OFFLINE_OR_UNREACHABLE -> ErrorDiagnosticCategory.OFFLINE_OR_UNREACHABLE
        ConnectivityKind.TIMEOUT                -> ErrorDiagnosticCategory.TIMEOUT
    },
)

internal fun AppFailure.Service.diagnosticFields(): DiagnosticFields = DiagnosticFields(
    category = when (kind) {
        ServiceKind.UNAVAILABLE     -> ErrorDiagnosticCategory.SERVICE_UNAVAILABLE
        ServiceKind.RATE_LIMITED    -> ErrorDiagnosticCategory.RATE_LIMITED
        ServiceKind.ACCESS_DENIED   -> ErrorDiagnosticCategory.ACCESS_DENIED
        ServiceKind.CLIENT_REJECTED -> ErrorDiagnosticCategory.CLIENT_REJECTED
        ServiceKind.OTHER_HTTP      -> ErrorDiagnosticCategory.OTHER_HTTP
    },
    status = httpStatus,
    reasonCode = backendReasonCode,
)

internal fun AppFailure.Authentication.diagnosticFields(): DiagnosticFields = DiagnosticFields(
    category = when (kind) {
        AuthenticationKind.REQUIRED -> ErrorDiagnosticCategory.AUTHENTICATION_REQUIRED
        AuthenticationKind.REVOKED  -> ErrorDiagnosticCategory.SESSION_REVOKED
    },
    status = httpStatus,
    reasonCode = backendReasonCode,
)

internal fun AppFailure.Storage.diagnosticFields(): DiagnosticFields = DiagnosticFields(
    when (kind) {
        StorageKind.READ    -> ErrorDiagnosticCategory.STORAGE_READ
        StorageKind.WRITE   -> ErrorDiagnosticCategory.STORAGE_WRITE
        StorageKind.CRYPTO  -> ErrorDiagnosticCategory.STORAGE_CRYPTO
        StorageKind.CORRUPT -> ErrorDiagnosticCategory.STORAGE_CORRUPT
        StorageKind.UNKNOWN -> ErrorDiagnosticCategory.STORAGE_UNKNOWN
    },
)
