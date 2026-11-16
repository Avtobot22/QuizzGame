package com.quizzgame.tv

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.ui.tags.TvDashboardTags
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.feature.quizentry.tv.ui.tags.TvQuizEntryTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal class CatalogScreen(provider: SemanticsNodeInteractionsProvider) : ComposeScreen<CatalogScreen>(provider) {
    val feedTab = KNode(provider) { hasTestTag(TvDashboardTags.tab(DashboardTabId.FEED)) }
    val libraryTab = KNode(provider) { hasTestTag(TvDashboardTags.tab(DashboardTabId.LIBRARY)) }
    val settingsTab = KNode(provider) { hasTestTag(TvDashboardTags.tab(DashboardTabId.SETTINGS)) }
    val signOut = KNode(provider) { hasTestTag(TvDashboardTags.SIGN_OUT) }
    val firstCard = KNode(provider) { hasTestTag(TvFeedTags.card(FIRST_CARD_ID)) }
    val details = KNode(provider) { hasTestTag(TvQuizEntryTags.CONTENT) }
    val bar = KNode(provider) { hasTestTag(TvDashboardTags.NAVIGATION) }

    companion object {
        const val FIRST_CARD_ID = "18e2b5b5-05e6-43f5-9580-2e88ec71303d"
    }
}
