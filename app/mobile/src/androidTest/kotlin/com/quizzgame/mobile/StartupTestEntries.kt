package com.quizzgame.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.feature.splash.mobile.navigation.MobileSplashRoute
import com.quizzgame.mobile.navigation.FixturePlaceholder
import com.quizzgame.mobile.navigation.MobileFixtureSignInDestination
import com.quizzgame.mobile.navigation.MobileNavigationContext
import com.quizzgame.mobile.ui.tags.MobileNavigationTags
import com.quizzgame.mobile.test.R as FixtureR

/** Root-policy tests substitute only the network-owning Splash entry. */
internal fun startupTestEntries(): FeatureEntries<MobileNavigationContext> = FeatureEntries { scope, context ->
    scope.entry<MobileSplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.SPLASH)) {
            FixturePlaceholder(
                R.string.splash_placeholder,
                listOf(
                    FixtureR.string.fixture_signin to { current.onSignIn() },
                    FixtureR.string.fixture_continue to { current.onContentReady() },
                ),
            )
        }
    }
    scope.entry<SignInRoute> {
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.SIGN_IN)) {
            MobileFixtureSignInDestination(onContinue = { context.value.onRestartSplash() })
        }
    }
}

internal fun fixtureContentEntries(): FeatureEntries<MobileNavigationContext> = FeatureEntries { scope, context ->
    scope.entry<com.quizzgame.feature.feed.mobile.navigation.ExploreRoute> {
        androidx.compose.material3.Text("Fixture")
    }
    scope.entry<com.quizzgame.feature.dashboard.mobile.navigation.LibraryRoute> {
        androidx.compose.material3.Text("Fixture")
    }
    scope.entry<com.quizzgame.feature.account.mobile.navigation.ProfileRoute> {
        androidx.compose.material3.Text("Fixture")
    }
    scope.entry<com.quizzgame.feature.play.mobile.navigation.PlayRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.PLAY)) {
            com.quizzgame.mobile.navigation.MobileFixturePlayDestination(
                current.onLogout,
            )
        }
    }
}
