package com.quizzgame.shared.room.data

import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.network.api.ApiWebSockets
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.room.data.admission.RoomAdmission
import com.quizzgame.shared.room.data.api.AcquiredRoom
import com.quizzgame.shared.room.data.dto.admission.CreateRoomResponse
import com.quizzgame.shared.room.data.mapper.toDomain
import com.quizzgame.shared.room.data.session.RoomSessionCoordinator
import com.quizzgame.shared.room.data.storage.RoomCredential
import com.quizzgame.shared.room.data.storage.RoomCredentialStore
import com.quizzgame.shared.room.data.storage.StoredRoom
import com.quizzgame.shared.room.data.stream.RoomStreamConnector
import com.quizzgame.shared.room.data.stream.RoomStreamDecoder
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.repository.RoomAccountState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class RoomSessionCoordinatorTest {
    @Test
    fun anOldLobbyKeepsItsTerminalStateWhenAnotherRoomOpens() = runTest {
        RoomHttpFixture().use { http ->
            val fixture = Fixture(http, StandardTestDispatcher(testScheduler))
            val first = fixture.acquired(OWNER)
            fixture.coordinator.restore()
            fixture.coordinator.persist(first)
            val old = (fixture.coordinator.enterLobby(first.room.id) as RoomResult.Success).value

            fixture.account.value = SessionState.Guest
            runCurrent()
            val next = fixture.acquired(null)
            fixture.coordinator.persist(next)
            val current = (fixture.coordinator.enterLobby(next.room.id) as RoomResult.Success).value
            fixture.coordinator.exitLobby(old)

            assertEquals(
                RoomStreamState.Ended(first.room.id, RoomFailureReason.ACCOUNT_CHANGED),
                old.states.value,
            )
            assertEquals(RoomStreamState.Connecting, current.states.value)
            fixture.coordinator.exitLobby(current)
        }
    }

    @Test
    fun discardsCredentialsWhenTheAccountChangesDuringStorageWrite() = runTest {
        RoomHttpFixture().use { http ->
            val fixture = Fixture(http, StandardTestDispatcher(testScheduler))
            fixture.store.onSave = { fixture.account.value = SessionState.Guest }
            fixture.coordinator.restore()

            val result = fixture.coordinator.persist(fixture.acquired(OWNER)) as RoomResult.Failed

            assertEquals(RoomFailureReason.ACCOUNT_CHANGED, result.failure.reason)
            assertNull(fixture.store.saved)
            assertNull(fixture.coordinator.session.value)
        }
    }

    @Test
    fun reusesAnUncertainAdmissionKeyUntilTheServerGivesADefiniteAnswer() = runTest {
        RoomHttpFixture().use { http ->
            val fixture = Fixture(http, StandardTestDispatcher(testScheduler))
            val repository = RoomAdmission(fixture.coordinator, http.remote, fixture.store, fixture.accounts)
            fixture.account.value = SessionState.Guest
            val entry = fixture.acquired(null).snapshot.entry.id.toString()

            http.reply(504)
            assertTrue((repository.create(entry) as RoomResult.Failed).failure.uncertain)
            val first = http.request()
            http.reply(409)
            assertTrue(!(repository.create(entry) as RoomResult.Failed).failure.uncertain)
            val retry = http.request()
            http.reply(504)
            repository.create(entry)
            val next = http.request()

            assertEquals(first.getHeader("Idempotency-Key"), retry.getHeader("Idempotency-Key"))
            assertEquals(first.body.readUtf8(), retry.body.readUtf8())
            assertNotEquals(first.getHeader("Idempotency-Key"), next.getHeader("Idempotency-Key"))
        }
    }

    private class Fixture(private val http: RoomHttpFixture, dispatcher: CoroutineDispatcher) {
        val store = MemoryStore()
        val account = MutableStateFlow<SessionState>(SessionState.Authenticated(OWNER))
        val accounts = RoomAccountState { account }
        val coordinator = RoomSessionCoordinator(
            store,
            http.remote,
            RoomStreamConnector(ApiWebSockets { _, _ -> null }, http.json, RoomStreamDecoder(http.json, http.links)),
            accounts,
            object : DispatcherProvider {
                override val default = dispatcher
                override val io = dispatcher
            },
        )

        fun acquired(owner: Long?): AcquiredRoom {
            val dto = http.json.decodeFromString<CreateRoomResponse>(fixture("tv-create-response"))
            val id = RoomId(UUID.randomUUID())
            val snapshot = dto.room.toDomain(RoomRole.HOST, http.links).copy(roomId = id)
            val credential = RoomCredential(RoomRole.HOST, "fixture", Instant.now().plus(1, ChronoUnit.DAYS))
            return AcquiredRoom(StoredRoom(id, credential, null, owner), snapshot)
        }
    }

    private class MemoryStore : RoomCredentialStore {
        var saved: StoredRoom? = null
        var onSave: () -> Unit = {}
        private val installation = UUID.randomUUID().toString()

        override suspend fun load(): StoredRoom? = saved

        override suspend fun save(room: StoredRoom) {
            saved = room
            onSave()
        }

        override suspend fun clear() {
            saved = null
        }

        override suspend fun installationId(): String = installation
    }

    private companion object {
        const val OWNER = 7L
    }
}
