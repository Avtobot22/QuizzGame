package com.quizzgame.feature.feed.mobile.navigation

import androidx.compose.runtime.State
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.feed.mobile.ui.MobileFeedScreen

data class MobileFeedCallbacks(val onOpenEntry: (String) -> Boolean, val onSignIn: () -> Unit)
object MobileFeedEntries : FeatureEntries<MobileFeedCallbacks> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<MobileFeedCallbacks>) {
        scope.entry<ExploreRoute> {
            MobileFeedScreen({ context.value.onOpenEntry(it) }, { context.value.onSignIn() })
        }
    }
}
