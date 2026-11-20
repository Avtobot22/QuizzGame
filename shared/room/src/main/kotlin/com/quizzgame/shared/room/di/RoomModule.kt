package com.quizzgame.shared.room.di

import com.quizzgame.shared.room.data.admission.RoomAdmission
import com.quizzgame.shared.room.data.session.RoomSessionRepositoryImpl
import com.quizzgame.shared.room.data.storage.EncryptedRoomCredentialStore
import com.quizzgame.shared.room.data.storage.RoomCredentialStore
import com.quizzgame.shared.room.domain.repository.RoomRepository
import com.quizzgame.shared.room.domain.repository.RoomSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface RoomModule {
    @Binds
    fun repository(implementation: RoomAdmission): RoomRepository

    @Binds
    fun sessions(implementation: RoomSessionRepositoryImpl): RoomSessionRepository

    @Binds
    fun store(implementation: EncryptedRoomCredentialStore): RoomCredentialStore
}
