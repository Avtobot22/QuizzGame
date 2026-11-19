package com.quizzgame.tv.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.navigation.AuthReturnTarget
import com.quizzgame.app.navigation.PendingAuthReturn
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.withContext
import com.quizzgame.feature.account.tv.navigation.TvProfileRoute
import com.quizzgame.feature.account.tv.ui.TvProfileScreen
import com.quizzgame.feature.auth.tv.navigation.TvSignInCallbacks
import com.quizzgame.feature.auth.tv.navigation.TvSignInEntries
import com.quizzgame.feature.splash.tv.navigation.TvSplashCallbacks
import com.quizzgame.feature.splash.tv.navigation.TvSplashEntries
import com.quizzgame.tv.navigation.routes.DashboardRoute
import com.quizzgame.tv.ui.tags.TvNavigationTags
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

internal data class TvNavigationContext(
    val onContentReady: () -> Boolean,
    val onSignIn: () -> Boolean,
    val onRestartSplash: () -> Boolean,
    val onLogout: () -> Unit,
    val onCancelAuth: () -> Unit = {},
    val authRecovery: Boolean = false,
    val onAuthFor: (AuthReturnTarget) -> Boolean = { onSignIn() },
    val onProfile: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val authenticated: Boolean = false,
    val openSettings: Boolean = false,
    val settingsHandled: () -> Unit = {},
    val authReturn: PendingAuthReturn? = null,
    val acknowledgeReturn: (java.util.UUID) -> Unit = {},
)

internal fun EntryProviderScope<NavKey>.tvStartupEntries(context: State<TvNavigationContext>) {
    TvSplashEntries.withContext<TvNavigationContext, TvSplashCallbacks> {
        TvSplashCallbacks(it.onContentReady, it.onSignIn)
    }.register(this, context)
    TvSignInEntries.withContext<TvNavigationContext, TvSignInCallbacks> {
        TvSignInCallbacks(it.onCancelAuth, it.authRecovery)
    }.register(this, context)
}

internal fun EntryProviderScope<NavKey>.tvContentEntries(context: State<TvNavigationContext>) {
    entry<TvProfileRoute> {
        TvProfileScreen(
            { context.value.onAuthFor(AuthReturnTarget.Profile) },
            context.value.onLogout,
            context.value.onSettings,
        )
    }
    entry<DashboardRoute> {
        val current = context.value
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.DASHBOARD)) {
            TvCatalogDashboard(current)
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
