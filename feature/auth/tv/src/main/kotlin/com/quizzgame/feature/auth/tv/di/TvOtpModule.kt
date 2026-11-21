package com.quizzgame.feature.auth.tv.di

import android.os.SystemClock
import com.quizzgame.shared.auth.presentation.OtpSignInEnvironment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import java.time.Instant

@Module
@InstallIn(ViewModelComponent::class)
internal object TvOtpModule {
    @Provides
    fun otpSignInEnvironment(): OtpSignInEnvironment =
        OtpSignInEnvironment(SystemClock::elapsedRealtime, Instant::now)
}
