package com.quizzgame.tv.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.withContext
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.feature.splash.tv.navigation.TvSplashCallbacks
import com.quizzgame.feature.splash.tv.navigation.TvSplashEntries
import com.quizzgame.tv.navigation.routes.DashboardRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

internal data class TvNavigationContext(
    val dashboardGraphs: Set<FeatureEntries<DashboardNavigationContext>>,
    val onContentReady: () -> Boolean,
    val onSignIn: () -> Boolean,
    val onRestartSplash: () -> Boolean,
    val onLogout: () -> Unit,
)

internal fun EntryProviderScope<NavKey>.tvStartupEntries(context: State<TvNavigationContext>) {
    TvSplashEntries.withContext<TvNavigationContext, TvSplashCallbacks> {
        TvSplashCallbacks(it.onContentReady, it.onSignIn)
    }.register(this, context)
    entry<SignInRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SIGN_IN)) {
            TvSignInDestination(onContinue = current.onRestartSplash)
        }
    }
}

internal fun EntryProviderScope<NavKey>.tvContentEntries(context: State<TvNavigationContext>) {
    entry<DashboardRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.DASHBOARD)) {
            TvDashboardDestination(current.onLogout, current.dashboardGraphs)
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object TvNavigationModule {
    @Provides
    @IntoSet
    fun startupEntries(): FeatureEntries<TvNavigationContext> =
        FeatureEntries { scope, context -> scope.tvStartupEntries(context) }

    @Provides
    @IntoSet
    fun contentEntries(): FeatureEntries<TvNavigationContext> =
        FeatureEntries { scope, context -> scope.tvContentEntries(context) }
}
