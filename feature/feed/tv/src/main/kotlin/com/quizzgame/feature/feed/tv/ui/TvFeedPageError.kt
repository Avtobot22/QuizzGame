package com.quizzgame.feature.feed.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorBanner
import com.quizzgame.core.error.ui.tv.policy.TvErrorDecisionPolicy
import com.quizzgame.feature.feed.tv.domain.FeedPagingException
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags

@Composable
internal fun TvFeedPageError(
    error: Throwable,
    hasContent: Boolean,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    operation: OperationKind = OperationKind.USER_COMMAND,
    tag: String = TvFeedTags.RETRY,
) {
    var dismissed by remember(error) { mutableStateOf(false) }
    if (dismissed) return
    val failure = (error as? FeedPagingException)?.failure ?: AppFailure.InvalidServerData
    val decision = TvErrorDecisionPolicy().decide(
        failure,
        ErrorOperationContext(operation, hasContent, RetrySafety.SAFE_TO_REPEAT),
    )
    val spec = if (operation == OperationKind.USER_REFRESH) {
        decision
    } else {
        decision.copy(actions = decision.actions.filterNot { it == ErrorActionKind.DISMISS })
    }
    androidx.compose.foundation.layout.Box(modifier.testTag(tag)) {
        if (spec.surface == ErrorSurface.BANNER) {
            QuizzTvErrorBanner(spec, onAction = { action ->
                when (action) {
                    ErrorActionKind.RETRY -> onRetry()
                    ErrorActionKind.SIGN_IN -> onSignIn()
                    ErrorActionKind.DISMISS -> dismissed = true
                    else -> Unit
                }
            })
        } else {
            TvFeedError(spec, onRetry, onSignIn)
        }
    }
}
