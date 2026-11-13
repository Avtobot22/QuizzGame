package com.quizzgame.tv.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.routes.DashboardRoute
import com.quizzgame.app.routes.SignInRoute
import com.quizzgame.app.routes.SplashRoute
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.splash.tv.ui.TvSplashScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

internal data class TvNavigationContext(
    val dashboardGraphs: Set<FeatureEntries<DashboardNavigationContext>>,
    val navigate: (NavAction) -> Unit,
    val onLogout: () -> Unit,
)

internal fun EntryProviderScope<NavKey>.tvStartupEntries(context: State<TvNavigationContext>) {
    entry<SplashRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SPLASH)) {
            val activity = LocalActivity.current
            TvSplashScreen(
                onDashboard = { current.navigate(NavAction.ResetTo(DashboardRoute)) },
                onSignIn = { current.navigate(NavAction.ResetTo(SignInRoute)) },
                onExit = { activity?.finish() },
            )
        }
    }
    entry<SignInRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.SIGN_IN)) {
            TvSignInDestination(onContinue = { current.navigate(NavAction.ResetTo(SplashRoute)) })
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
