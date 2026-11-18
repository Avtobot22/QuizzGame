package com.quizzgame.feature.feed.mobile.ui.status

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.operation.ErrorOperationContext
import com.quizzgame.core.error.operation.OperationKind
import com.quizzgame.core.error.operation.RetrySafety
import com.quizzgame.core.error.policy.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorCard
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorFullScreen
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.shared.feed.domain.FeedPagingException

@Composable
internal fun MobileFeedPageError(
    error: Throwable,
    hasContent: Boolean,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = MobileFeedTags.ERROR,
    compact: Boolean = false,
) {
    val failure = (error as? FeedPagingException)?.failure ?: AppFailure.InvalidServerData
    val spec = DefaultErrorDecisionPolicy().decide(
        failure,
        ErrorOperationContext(OperationKind.USER_REFRESH, hasContent, RetrySafety.SAFE_TO_REPEAT),
    ).let { it.copy(actions = it.actions.filterNot { action -> action == ErrorActionKind.DISMISS }) }
    val onAction: (ErrorActionKind) -> Unit = { action ->
        when (action) {
            ErrorActionKind.RETRY -> onRetry()
            ErrorActionKind.SIGN_IN -> onSignIn()
            else -> Unit
        }
    }
    val cardModifier = if (compact) modifier else modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    if (hasContent) {
        QuizzMobileErrorCard(
            spec,
            onAction,
            cardModifier.testTag(tag),
            compact = compact,
        )
    } else {
        QuizzMobileErrorFullScreen(spec, onAction, modifier.testTag(tag))
    }
}
