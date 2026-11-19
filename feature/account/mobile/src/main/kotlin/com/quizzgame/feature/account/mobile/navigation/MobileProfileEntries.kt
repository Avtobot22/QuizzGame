package com.quizzgame.feature.account.mobile.navigation

import androidx.compose.runtime.State
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.account.mobile.ui.MobileProfileScreen

data class MobileProfileCallbacks(val onSignIn: () -> Unit, val onSignOut: () -> Unit)

object MobileProfileEntries : FeatureEntries<MobileProfileCallbacks> {
    override fun register(scope: EntryProviderScope<NavKey>, context: State<MobileProfileCallbacks>) {
        scope.entry<ProfileRoute> {
            MobileProfileScreen({ context.value.onSignIn() }, { context.value.onSignOut() })
        }
    }
}
