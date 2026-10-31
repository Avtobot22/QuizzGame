package com.quizzgame.core.error

internal fun AppFailure.diagnosticFields(): DiagnosticFields = when (this) {
    is AppFailure.Connectivity   -> diagnosticFields()
    is AppFailure.Service        -> diagnosticFields()
    is AppFailure.Authentication -> diagnosticFields()
    AppFailure.InvalidServerData -> DiagnosticFields(ErrorDiagnosticCategory.INVALID_SERVER_DATA)
    is AppFailure.Storage        -> diagnosticFields()
    is DevelopmentFailure        -> DiagnosticFields(
        when (kind) {
            DevelopmentKind.MOCK_NO_MATCH         -> ErrorDiagnosticCategory.MOCK_NO_MATCH
            DevelopmentKind.MOCK_INVALID_REQUEST  -> ErrorDiagnosticCategory.MOCK_INVALID_REQUEST
            DevelopmentKind.MOCK_CALLBACK         -> ErrorDiagnosticCategory.MOCK_CALLBACK
            DevelopmentKind.MOCK_INVALID_RESPONSE -> ErrorDiagnosticCategory.MOCK_INVALID_RESPONSE
        },
    )
}
