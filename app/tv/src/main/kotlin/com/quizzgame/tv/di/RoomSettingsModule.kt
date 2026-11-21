package com.quizzgame.tv.di

import com.quizzgame.app.startup.AppStartup
import com.quizzgame.shared.room.domain.entity.admission.RoomEndpointSettings
import com.quizzgame.shared.room.domain.repository.RoomAccountState
import com.quizzgame.tv.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object RoomSettingsModule {
    @Provides
    fun endpointSettings(): RoomEndpointSettings = RoomEndpointSettings(
        BuildConfig.ROOM_JOIN_HOST.takeUnless { it == "disabled.invalid" }.orEmpty(),
    )

    @Provides
    fun accountState(startup: AppStartup): RoomAccountState = RoomAccountState { startup.sessionState }
}
