package com.quizzgame.feature.auth.mobile.di

import android.os.SystemClock
import com.quizzgame.shared.auth.presentation.OtpSignInEnvironment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import java.time.Instant

@Module
@InstallIn(ViewModelComponent::class)
internal object MobileOtpModule {
    @Provides
    fun otpSignInEnvironment(): OtpSignInEnvironment =
        OtpSignInEnvironment(SystemClock::elapsedRealtime, Instant::now)
}
