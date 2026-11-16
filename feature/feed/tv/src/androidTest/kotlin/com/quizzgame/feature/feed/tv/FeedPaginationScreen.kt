package com.quizzgame.feature.feed.tv

import android.view.KeyEvent
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class FeedPaginationScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<FeedPaginationScreen>(provider) {
    val firstCard = KNode(provider) { hasTestTag(TvFeedTags.card("quiz-0")) }
    val refresh = KNode(provider) { hasTestTag(TvFeedTags.REFRESH) }

    fun assertDecorativeLoading(tag: String) {
        provider.onNodeWithTag(tag).assertHasNoClickAction()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.RequestFocus))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate,
                ),
            )
    }

    fun openCardAndMovePastPlaceholders() {
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_CENTER))
        onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DPAD_RIGHT))
        refresh.assertIsFocused()
    }
}
