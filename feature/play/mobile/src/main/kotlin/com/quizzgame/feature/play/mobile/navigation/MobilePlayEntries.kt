package com.quizzgame.feature.play.mobile.navigation

import androidx.compose.runtime.State
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.play.mobile.ui.MobilePlayScreen

data class MobilePlayCallbacks(
    val openLobby: (String) -> Boolean,
    val signIn: (String, String) -> Boolean,
)

object MobilePlayEntries : FeatureEntries<MobilePlayCallbacks> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<MobilePlayCallbacks>) {
        scope.entry<PlayRoute> { MobilePlayScreen(context.value) }
        scope.entry<MobileJoinRoute> { route -> MobilePlayScreen(context.value, draft = route) }
    }
}
