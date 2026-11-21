package com.quizzgame.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.feature.splash.tv.navigation.TvSplashRoute
import com.quizzgame.tv.navigation.FixturePlaceholder
import com.quizzgame.tv.navigation.TvFixtureSignInDestination
import com.quizzgame.tv.navigation.TvNavigationContext
import com.quizzgame.tv.ui.tags.TvNavigationTags
import com.quizzgame.tv.test.R as FixtureR

/** Root-policy tests substitute only the network-owning Splash entry. */
internal fun startupTestEntries(): FeatureEntries<TvNavigationContext> = FeatureEntries { scope, context ->
    scope.entry<TvSplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SPLASH)) {
            FixturePlaceholder(
                FixtureR.string.fixture_splash_title,
                listOf(
                    FixtureR.string.fixture_signin to { current.onSignIn() },
                    FixtureR.string.fixture_continue to { current.onContentReady() },
                ),
            )
        }
    }
    scope.entry<SignInRoute> {
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SIGN_IN)) {
            TvFixtureSignInDestination(onContinue = { context.value.onRestartSplash() })
        }
    }
}
