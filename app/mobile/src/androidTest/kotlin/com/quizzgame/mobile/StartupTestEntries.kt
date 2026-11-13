package com.quizzgame.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quizzgame.app.routes.JoinRoomRoute
import com.quizzgame.app.routes.SignInRoute
import com.quizzgame.app.routes.SplashRoute
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.mobile.navigation.DemoPlaceholder
import com.quizzgame.mobile.navigation.MobileNavigationContext
import com.quizzgame.mobile.navigation.MobileNavigationTags
import com.quizzgame.mobile.navigation.MobileSignInDestination

/** Root-policy tests substitute only the network-owning Splash entry. */
internal fun startupTestEntries(): FeatureEntries<MobileNavigationContext> = FeatureEntries { scope, context ->
    scope.entry<SplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.SPLASH)) {
            DemoPlaceholder(
                R.string.splash_placeholder,
                listOf(
                    R.string.demo_signin to { current.navigate(NavAction.ResetTo(SignInRoute)) },
                    R.string.demo_continue to { current.navigate(NavAction.ResetTo(JoinRoomRoute)) },
                ),
            )
        }
    }
    scope.entry<SignInRoute> {
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.SIGN_IN)) {
            MobileSignInDestination(onContinue = { context.value.navigate(NavAction.ResetTo(SplashRoute)) })
        }
    }
}
