package com.quizzgame.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.feature.splash.tv.navigation.TvSplashRoute
import com.quizzgame.tv.navigation.DemoPlaceholder
import com.quizzgame.tv.navigation.TvNavigationContext
import com.quizzgame.tv.navigation.TvNavigationTags
import com.quizzgame.tv.navigation.TvSignInDestination

/** Root-policy tests substitute only the network-owning Splash entry. */
internal fun startupTestEntries(): FeatureEntries<TvNavigationContext> = FeatureEntries { scope, context ->
    scope.entry<TvSplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SPLASH)) {
            DemoPlaceholder(
                R.string.splash_placeholder,
                listOf(
                    R.string.demo_signin to { current.onSignIn() },
                    R.string.demo_continue to { current.onContentReady() },
                ),
            )
        }
    }
    scope.entry<SignInRoute> {
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SIGN_IN)) {
            TvSignInDestination(onContinue = { context.value.onRestartSplash() })
        }
    }
}
