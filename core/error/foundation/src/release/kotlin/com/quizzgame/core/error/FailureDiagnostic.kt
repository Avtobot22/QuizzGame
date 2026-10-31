package com.quizzgame.core.error

internal fun AppFailure.diagnosticFields(): DiagnosticFields = when (this) {
    is AppFailure.Connectivity   -> diagnosticFields()
    is AppFailure.Service        -> diagnosticFields()
    is AppFailure.Authentication -> diagnosticFields()
    AppFailure.InvalidServerData -> DiagnosticFields(ErrorDiagnosticCategory.INVALID_SERVER_DATA)
    is AppFailure.Storage        -> diagnosticFields()
}
