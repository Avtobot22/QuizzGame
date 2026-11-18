package com.quizzgame.feature.quizentry.mobile.navigation

import androidx.compose.runtime.State
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.chrome.NavigationChromePolicy
import com.quizzgame.core.navigation.chrome.navigationChrome
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.quizentry.mobile.ui.MobileQuizEntryScreen

data class MobileQuizEntryCallbacks(val onBack: () -> Unit, val onPlay: () -> Boolean, val onSignIn: () -> Unit)
object MobileQuizEntryEntries : FeatureEntries<MobileQuizEntryCallbacks> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<MobileQuizEntryCallbacks>) {
        scope.entry<MobileQuizEntryRoute>(metadata = navigationChrome(NavigationChromePolicy.HIDDEN)) { route ->
            MobileQuizEntryScreen(
                route.entryId,
                { context.value.onBack() },
                { context.value.onPlay() },
                { context.value.onSignIn() },
            )
        }
    }
}
