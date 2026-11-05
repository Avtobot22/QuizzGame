package com.quizzgame.tv.dashboard.demo

import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.navigation.DashboardNavigationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object DemoDashboardEntries {
    @Provides
    @IntoSet
    fun tabs(): FeatureEntries<DashboardNavigationContext> = FeatureEntries { scope, context ->
        scope.entry<DashboardDestination.TabRoot> { route ->
            val current = context.value
            DemoTabContent(route.tab, current.requestFocus, current.open)
        }
    }

    @Provides
    @IntoSet
    fun details(): FeatureEntries<DashboardNavigationContext> = FeatureEntries { scope, context ->
        scope.entry<DashboardDestination.QuizDetail> { route -> DemoQuizDetail(route.quizId, context.value.back) }
    }
}
