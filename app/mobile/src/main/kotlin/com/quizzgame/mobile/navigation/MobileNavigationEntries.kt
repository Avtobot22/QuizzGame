package com.quizzgame.mobile.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.routes.JoinRoomRoute
import com.quizzgame.app.routes.SignInRoute
import com.quizzgame.app.routes.SplashRoute
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.splash.mobile.ui.MobileSplashScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

internal data class MobileNavigationContext(
    val roomCode: String,
    val onRoomCodeChange: (String) -> Unit,
    val navigate: (NavAction) -> Unit,
    val onLogout: () -> Unit,
)

internal fun EntryProviderScope<NavKey>.mobileStartupEntries(context: State<MobileNavigationContext>) {
    entry<SplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.SPLASH)) {
            val activity = LocalActivity.current
            MobileSplashScreen(
                onJoinRoom = { current.navigate(NavAction.ResetTo(JoinRoomRoute)) },
                onSignIn = { current.navigate(NavAction.ResetTo(SignInRoute)) },
                onExit = { activity?.finish() },
            )
        }
    }
    entry<SignInRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.SIGN_IN)) {
            MobileSignInDestination(onContinue = { current.navigate(NavAction.ResetTo(SplashRoute)) })
        }
    }
}

internal fun EntryProviderScope<NavKey>.mobileContentEntries(context: State<MobileNavigationContext>) {
    entry<JoinRoomRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.JOIN_ROOM)) {
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
