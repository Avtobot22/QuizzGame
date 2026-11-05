package com.quizzgame.tv.navigation

import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.registration.FeatureEntries
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.Multibinds

internal data class DashboardNavigationContext(
    val requestFocus: Boolean,
    val open: (NavKey) -> Unit,
    val back: () -> Unit,
)

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class DashboardNavigationModule {
    @Multibinds
    abstract fun dashboardEntries(): Set<FeatureEntries<DashboardNavigationContext>>
}
