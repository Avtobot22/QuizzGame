package com.quizzgame.feature.quizentry.mobile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorFullScreen
import com.quizzgame.feature.quizentry.mobile.R
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryIntent
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryState
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags

@Composable
internal fun MobileQuizEntryStateContent(
    state: MobileQuizEntryState,
    onIntent: (MobileQuizEntryIntent) -> Unit,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuizzScreenScaffold(modifier.testTag(MobileQuizEntryTags.ROOT), topBar = {
        QuizzTopBar(stringResource(R.string.entry_title), onBack = onBack)
    }) { padding ->
        when (val current = state) {
            MobileQuizEntryState.Loading -> MobileQuizEntrySkeleton(Modifier.padding(padding))
            is MobileQuizEntryState.Content -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                MobileQuizEntryContent(
                    current.value,
                    { onIntent(MobileQuizEntryIntent.Connect) },
                    Modifier.padding(padding),
                )
            }
            is MobileQuizEntryState.Error -> {
                val spec = current.spec
                QuizzMobileErrorFullScreen(
                    spec.copy(actions = (spec.actions + ErrorActionKind.GO_BACK).distinct()),
                    onAction = { action ->
                        when (action) {
                            ErrorActionKind.RETRY -> onIntent(MobileQuizEntryIntent.Retry)
                            ErrorActionKind.SIGN_IN -> onSignIn()
                            ErrorActionKind.GO_BACK -> onBack()
                            else -> Unit
                        }
                    },
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}
