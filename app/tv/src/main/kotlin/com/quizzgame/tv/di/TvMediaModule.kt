package com.quizzgame.tv.di

import android.content.Context
import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.designsystem.tv.media.TvMediaLoader
import com.quizzgame.tv.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TvMediaModule {
    @Provides
    @Singleton
    fun imageLoader(@ApplicationContext context: Context, dispatchers: DispatcherProvider): TvMediaLoader =
        TvMediaLoader(
            context,
            BuildConfig.CATALOG_IMAGE_HOSTS.split(',').filter { it.isNotBlank() }.toSet(),
            dispatchers,
        )
}
