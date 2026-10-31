package com.quizzgame.core.error

import com.quizzgame.core.network.MockFailureReason

internal fun classifyDiagnosticFailure(reason: MockFailureReason): AppFailure = DevelopmentFailure(
    when (reason) {
        MockFailureReason.NO_MATCHING_RULE -> DevelopmentKind.MOCK_NO_MATCH
        MockFailureReason.INVALID_REQUEST  -> DevelopmentKind.MOCK_INVALID_REQUEST
        MockFailureReason.CALLBACK_FAILURE -> DevelopmentKind.MOCK_CALLBACK
        MockFailureReason.INVALID_RESPONSE -> DevelopmentKind.MOCK_INVALID_RESPONSE
    },
)
