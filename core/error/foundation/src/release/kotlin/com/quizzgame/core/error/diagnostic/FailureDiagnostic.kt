package com.quizzgame.core.error.diagnostic

import com.quizzgame.core.error.failure.AppFailure

internal fun AppFailure.diagnosticFields(): DiagnosticFields = when (this) {
    is AppFailure.Connectivity      -> diagnosticFields()
    is AppFailure.Service           -> diagnosticFields()
    is AppFailure.Authentication    -> diagnosticFields()
    AppFailure.InvalidServerData    -> DiagnosticFields(ErrorDiagnosticCategory.INVALID_SERVER_DATA)
    is AppFailure.Storage           -> diagnosticFields()
    AppFailure.Configuration        -> DiagnosticFields(ErrorDiagnosticCategory.CONFIGURATION)
    AppFailure.OperationInterrupted -> DiagnosticFields(ErrorDiagnosticCategory.OPERATION_INTERRUPTED)
}
