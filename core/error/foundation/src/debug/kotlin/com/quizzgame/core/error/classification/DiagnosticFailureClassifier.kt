package com.quizzgame.core.error.classification

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.DevelopmentFailure
import com.quizzgame.core.error.failure.DevelopmentKind
import com.quizzgame.core.network.diagnostic.MockFailureReason

internal fun classifyDiagnosticFailure(reason: MockFailureReason): AppFailure = DevelopmentFailure(
    when (reason) {
        MockFailureReason.NO_MATCHING_RULE -> DevelopmentKind.MOCK_NO_MATCH
        MockFailureReason.INVALID_REQUEST  -> DevelopmentKind.MOCK_INVALID_REQUEST
        MockFailureReason.CALLBACK_FAILURE -> DevelopmentKind.MOCK_CALLBACK
        MockFailureReason.INVALID_RESPONSE -> DevelopmentKind.MOCK_INVALID_RESPONSE
    },
)
