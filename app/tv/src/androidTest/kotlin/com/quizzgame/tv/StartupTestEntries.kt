package com.quizzgame.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quizzgame.app.routes.DashboardRoute
import com.quizzgame.app.routes.SignInRoute
import com.quizzgame.app.routes.SplashRoute
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.navigation.DemoPlaceholder
import com.quizzgame.tv.navigation.TvNavigationContext
import com.quizzgame.tv.navigation.TvNavigationTags
import com.quizzgame.tv.navigation.TvSignInDestination

/** Root-policy tests substitute only the network-owning Splash entry. */
internal fun startupTestEntries(): FeatureEntries<TvNavigationContext> = FeatureEntries { scope, context ->
    scope.entry<SplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SPLASH)) {
            DemoPlaceholder(
                R.string.splash_placeholder,
                listOf(
                    R.string.demo_signin to { current.navigate(NavAction.ResetTo(SignInRoute)) },
                    R.string.demo_continue to { current.navigate(NavAction.ResetTo(DashboardRoute)) },
                ),
            )
        }
    }
    scope.entry<SignInRoute> {
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SIGN_IN)) {
            TvSignInDestination(onContinue = { context.value.navigate(NavAction.ResetTo(SplashRoute)) })
        }
    }
}
