package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.GuestAccess
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.routes.JoinRoomRoute
import com.quizzgame.core.navigation.routes.SignInRoute
import com.quizzgame.core.navigation.routes.SplashRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

internal data class MobileNavigationContext(
    val roomCode: String,
    val onRoomCodeChange: (String) -> Unit,
    val navigate: (NavAction) -> Unit,
    val onContinue: (GuestAccess) -> Unit,
    val onLogout: () -> Unit,
)

internal fun EntryProviderScope<NavKey>.mobileStartupEntries(context: State<MobileNavigationContext>) {
    entry<SplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag("splash")) {
            MobileSplashDestination({ current.navigate(NavAction.ResetTo(SignInRoute)) }, current.onContinue)
        }
    }
    entry<SignInRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag("signin")) {
            MobileSignInDestination(current.onContinue)
        }
    }
}

internal fun EntryProviderScope<NavKey>.mobileContentEntries(context: State<MobileNavigationContext>) {
    entry<JoinRoomRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag("join-room")) {
            MobileJoinRoomDestination(current.roomCode, current.onRoomCodeChange, current.onLogout)
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object MobileNavigationModule {
    @Provides
    @IntoSet
    fun startupEntries(): FeatureEntries<MobileNavigationContext> =
        FeatureEntries { scope, context -> scope.mobileStartupEntries(context) }

    @Provides
    @IntoSet
    fun contentEntries(): FeatureEntries<MobileNavigationContext> =
        FeatureEntries { scope, context -> scope.mobileContentEntries(context) }
}
