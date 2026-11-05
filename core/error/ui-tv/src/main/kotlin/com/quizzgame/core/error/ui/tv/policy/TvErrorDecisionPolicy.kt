package com.quizzgame.core.error.ui.tv.policy

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.policy.ErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface

/** Ошибки команд на TV остаются в состоянии экрана, чтобы действия были доступны с D-pad. */
class TvErrorDecisionPolicy : ErrorDecisionPolicy {
    private val common = DefaultErrorDecisionPolicy()

    override fun decide(failure: AppFailure, context: ErrorOperationContext): ErrorPresentationSpec {
        val spec = common.decide(failure, context)
        if (spec.surface != ErrorSurface.TRANSIENT) return spec
        return spec.copy(surface = if (context.hasUsableContent) ErrorSurface.BANNER else ErrorSurface.INLINE)
    }
}
