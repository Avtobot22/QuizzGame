package com.quizzgame.shared.room.data.storage

internal interface RoomCredentialStore {
    suspend fun load(): StoredRoom?
    suspend fun save(room: StoredRoom)
    suspend fun clear()
    suspend fun installationId(): String
}
