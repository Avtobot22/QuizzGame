package com.quizzgame.feature.play.mobile.navigation

import androidx.compose.runtime.State
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.play.mobile.ui.MobilePlayScreen

object MobilePlayEntries : FeatureEntries<Unit> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<Unit>) {
        scope.entry<PlayRoute> { MobilePlayScreen() }
    }
}
