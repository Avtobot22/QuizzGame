package com.quizzgame.feature.splash.mobile.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.splash.mobile.ui.MobileSplashScreen
import com.quizzgame.feature.splash.mobile.ui.MobileSplashTags

data class MobileSplashCallbacks(val onContinue: () -> Boolean, val onSignIn: () -> Boolean)

object MobileSplashEntries : FeatureEntries<MobileSplashCallbacks> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<MobileSplashCallbacks>) {
        scope.entry<MobileSplashRoute> {
            val activity = LocalActivity.current
            MobileSplashScreen(
                onJoinRoom = { context.value.onContinue() },
                onSignIn = { context.value.onSignIn() },
                onExit = { activity?.finish() },
                modifier = Modifier.testTag(MobileSplashTags.ROOT),
            )
        }
    }
}
