package com.quizzgame.tv.di

import com.quizzgame.shared.auth.domain.entity.device.DeviceVerificationHost
import com.quizzgame.tv.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object TvDeviceAuthModule {
    @Provides
    fun deviceVerificationHost(): DeviceVerificationHost = DeviceVerificationHost(BuildConfig.DEVICE_VERIFICATION_HOST)
}
