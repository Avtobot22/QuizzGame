package com.quizzgame.app.navigation

import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.request.ExternalNavigationParser
import com.quizzgame.core.navigation.request.NavigationRequest
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Inject

class ExternalNavigationRouter @Inject constructor(
    private val parsers: Set<@JvmSuppressWildcards ExternalNavigationParser>,
) {
    fun parse(uri: String): NavigationRequest? = parsers.mapNotNull { it.parse(uri) }.singleOrNull()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ExternalNavigationModule {
    @Multibinds
    abstract fun parsers(): Set<ExternalNavigationParser>

    @Multibinds
    abstract fun policies(): Set<NavigationPolicy>
}
