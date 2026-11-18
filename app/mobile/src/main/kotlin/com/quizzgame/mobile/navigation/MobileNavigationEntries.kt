package com.quizzgame.mobile.navigation

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
import com.quizzgame.feature.dashboard.mobile.navigation.MobileAccountCallbacks
import com.quizzgame.feature.dashboard.mobile.navigation.MobileAccountEntries
import com.quizzgame.feature.feed.mobile.navigation.MobileFeedCallbacks
import com.quizzgame.feature.feed.mobile.navigation.MobileFeedEntries
import com.quizzgame.feature.play.mobile.navigation.MobilePlayEntries
import com.quizzgame.feature.quizentry.mobile.navigation.MobileQuizEntryCallbacks
import com.quizzgame.feature.quizentry.mobile.navigation.MobileQuizEntryEntries
import com.quizzgame.feature.splash.mobile.navigation.MobileSplashCallbacks
import com.quizzgame.feature.splash.mobile.navigation.MobileSplashEntries
import com.quizzgame.mobile.ui.tags.MobileNavigationTags
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

internal data class MobileNavigationContext(
    val onContentReady: () -> Boolean,
    val onSignIn: () -> Boolean,
    val onRestartSplash: () -> Boolean,
    val onLogout: () -> Unit,
    val onOpenEntry: (String) -> Boolean = { false },
    val onPlay: () -> Boolean = { false },
    val onBack: () -> Unit = {},
)

internal fun EntryProviderScope<NavKey>.mobileStartupEntries(context: State<MobileNavigationContext>) {
    MobileSplashEntries.withContext<MobileNavigationContext, MobileSplashCallbacks> {
        MobileSplashCallbacks(it.onContentReady, it.onSignIn)
    }.register(this, context)
    entry<SignInRoute> {
        Box(Modifier.fillMaxSize().testTag(MobileNavigationTags.SIGN_IN)) {
            MobileSignInDestination()
        }
    }
}

internal fun EntryProviderScope<NavKey>.mobileContentEntries(context: State<MobileNavigationContext>) {
    MobilePlayEntries.withContext<MobileNavigationContext, Unit> { Unit }.register(this, context)
    MobileFeedEntries.withContext<MobileNavigationContext, MobileFeedCallbacks> {
        MobileFeedCallbacks(it.onOpenEntry, { it.onSignIn() })
    }.register(this, context)
    MobileQuizEntryEntries.withContext<MobileNavigationContext, MobileQuizEntryCallbacks> {
        MobileQuizEntryCallbacks(it.onBack, it.onPlay, { it.onSignIn() })
    }.register(this, context)
    MobileAccountEntries.withContext<MobileNavigationContext, MobileAccountCallbacks> {
        MobileAccountCallbacks({ it.onSignIn() }, it.onLogout)
    }.register(this, context)
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
