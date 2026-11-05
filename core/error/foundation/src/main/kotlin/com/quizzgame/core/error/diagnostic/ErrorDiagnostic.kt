package com.quizzgame.core.error.diagnostic

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.AuthenticationKind
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.core.error.failure.ServiceKind
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.error.operation.ErrorOperationKey
import java.util.UUID

/**
 * В диагностику передаём только статический ключ операции, категорию и числовые коды.
 * Исходные исключения, backend-тексты, URL и данные сессии сюда не попадают.
 * Необязательный diagnosticId создаёт вызывающий код; он не влияет на показ ошибки.
 */
data class ErrorDiagnostic(
    val operationKey: ErrorOperationKey,
    val category: ErrorDiagnosticCategory,
    val status: Int? = null,
    val reasonCode: Int? = null,
    val diagnosticId: UUID? = null,
) {
    companion object {

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
        AuthenticationKind.REQUIRED, AuthenticationKind.RECOVERY_REQUIRED ->
            ErrorDiagnosticCategory.AUTHENTICATION_REQUIRED
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
