package com.quizzgame.tv

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class TvAuthTestScreen(provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TvAuthTestScreen>(provider) {
    val identifier = KNode(provider) { hasTestTag(TvSignInTags.IDENTIFIER) }
}
