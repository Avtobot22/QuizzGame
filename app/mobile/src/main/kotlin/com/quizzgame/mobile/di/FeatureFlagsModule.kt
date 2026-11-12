package com.quizzgame.mobile.di

import android.content.Context
import com.quizzgame.core.flags.AppFeatureDefinitions
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.flags.ExecutorFeatureFlagsSource
import com.quizzgame.core.flags.FeatureFlagsController
import com.quizzgame.core.flags.FeatureFlagsSdkAdapter
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.avtobot22.featureflags.FeatureFlagsReader
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Tag
import javax.inject.Singleton

internal interface MobileFlagsApi {
    @GET("api/mobile/feature-toggles")
    suspend fun flags(@Tag policy: RequestPolicy): ResponseBody
}

@Module
@InstallIn(SingletonComponent::class)
internal object FeatureFlagsModule {
    @Provides
    @Singleton
    fun adapter(@ApplicationContext context: Context, executor: ApiCallExecutor): FeatureFlagsSdkAdapter =
        FeatureFlagsSdkAdapter(
            ExecutorFeatureFlagsSource(executor, MobileFlagsApi::class.java) {
                flags(RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE))
            },
            FeatureFlagsSdkAdapter.bundled(context.applicationContext.assets),
            AppFeatureDefinitions.mobileGuest,
        )

    @Provides fun reader(adapter: FeatureFlagsSdkAdapter): FeatureFlagsReader = adapter.reader

    @Provides fun flags(adapter: FeatureFlagsSdkAdapter): AppFeatureFlags = adapter

    @Provides fun controller(adapter: FeatureFlagsSdkAdapter): FeatureFlagsController = adapter
}
