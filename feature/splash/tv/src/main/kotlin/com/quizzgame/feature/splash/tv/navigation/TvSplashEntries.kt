package com.quizzgame.feature.splash.tv.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.splash.tv.ui.TvSplashScreen
import com.quizzgame.feature.splash.tv.ui.TvSplashTags

data class TvSplashCallbacks(val onContinue: () -> Boolean, val onSignIn: () -> Boolean)

object TvSplashEntries : FeatureEntries<TvSplashCallbacks> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<TvSplashCallbacks>) {
        scope.entry<TvSplashRoute> {
            val activity = LocalActivity.current
            TvSplashScreen(
                onDashboard = { context.value.onContinue() },
                onSignIn = { context.value.onSignIn() },
                onExit = { activity?.finish() },
                modifier = Modifier.testTag(TvSplashTags.ROOT),
            )
        }
    }
}
