package com.quizzgame.feature.feed.tv.ui.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.shared.catalog.domain.entity.QuizCard

@Composable
internal fun TvRailCard(
    card: QuizCard,
    onOpen: (String) -> Unit,
    parallax: Boolean,
    onFocused: (String) -> Unit = {},
    restoreFocus: Boolean = false,
) {
    val focus = remember { FocusRequester() }
    TvQuizCard(
        card,
        onClick = { onOpen(card.entryId) },
        modifier = Modifier.testTag(TvFeedTags.card(card.id)).focusRequester(focus)
            .onFocusChanged { if (it.isFocused) onFocused(card.id) },
        parallax = parallax,
    )
    TvEntryFocus(focus, restoreFocus)
}
