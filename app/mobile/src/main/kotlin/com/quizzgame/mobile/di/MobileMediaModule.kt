package com.quizzgame.mobile.di

import android.content.Context
import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.media.android.CatalogMediaLoader
import com.quizzgame.mobile.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object MobileMediaModule {
    @Provides
    @Singleton
    fun imageLoader(@ApplicationContext context: Context, dispatchers: DispatcherProvider): CatalogMediaLoader =
        CatalogMediaLoader(
            context,
            BuildConfig.CATALOG_IMAGE_HOSTS.split(',').filter { it.isNotBlank() }.toSet(),
            dispatchers,
        )
}
