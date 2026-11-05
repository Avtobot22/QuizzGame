package com.quizzgame.core.error.policy

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.presentation.ErrorPresentationSpec

fun interface ErrorDecisionPolicy {
    fun decide(failure: AppFailure, context: ErrorOperationContext): ErrorPresentationSpec
}
