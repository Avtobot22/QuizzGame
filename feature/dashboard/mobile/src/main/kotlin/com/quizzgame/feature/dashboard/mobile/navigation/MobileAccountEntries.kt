package com.quizzgame.feature.dashboard.mobile.navigation

import androidx.compose.runtime.State
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.dashboard.mobile.ui.MobileAccountScreen

data class MobileAccountCallbacks(val onSignIn: () -> Unit, val onLogout: () -> Unit)
object MobileAccountEntries : FeatureEntries<MobileAccountCallbacks> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<MobileAccountCallbacks>) {
        scope.entry<LibraryRoute> {
            MobileAccountScreen(true, { context.value.onSignIn() }, { context.value.onLogout() })
        }
        scope.entry<ProfileRoute> {
            MobileAccountScreen(false, { context.value.onSignIn() }, { context.value.onLogout() })
        }
    }
}
