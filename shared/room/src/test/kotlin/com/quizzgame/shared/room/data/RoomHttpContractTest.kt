package com.quizzgame.shared.room.data

import com.quizzgame.core.network.auth.CredentialsProvider
import com.quizzgame.core.network.auth.CredentialsSnapshot
import com.quizzgame.shared.room.data.api.JoinAdmission
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RoomHttpContractTest {
    @Test
    fun createRetryUsesSameKeyAndBodyAndCloseUsesOnlyRoomCredential() = runBlocking {
        RoomHttpFixture().use { fixture ->
            val key = UUID.randomUUID()
            val install = UUID.randomUUID().toString()
            fixture.reply(504)
            assertTrue((fixture.remote.create(ENTRY, install, key, null) as RoomResult.Failed).failure.uncertain)
            fixture.reply(201, fixture("tv-create-response"))
            val room = (fixture.remote.create(ENTRY, install, key, null) as RoomResult.Success).value.room
            val first = fixture.request()
            val retry = fixture.request()
            assertEquals("/api/rooms", retry.path)
            assertEquals(key.toString(), first.getHeader("Idempotency-Key"))
            assertEquals(first.getHeader("Idempotency-Key"), retry.getHeader("Idempotency-Key"))
            assertEquals(first.body.readUtf8(), retry.body.readUtf8())
            assertNull(retry.getHeader("Authorization"))
            assertNull(retry.getHeader("X-Room-Token"))

            fixture.reply(204)
            assertTrue(fixture.remote.remove(room, RoomCommand.Close) is RoomResult.Success)
            val close = fixture.request()
            assertEquals("DELETE", close.method)
            assertEquals("/api/rooms/${room.id}", close.path)
            assertEquals(room.credential.token, close.getHeader("X-Room-Token"))
            assertNull(close.getHeader("Authorization"))
        }
    }

    @Test
    fun mapsRoomResponsesWithoutTreatingNetworkFailureAsExpiry() = runBlocking {
        RoomHttpFixture().use { fixture ->
            val cases = listOf(
                Triple(401, 32001, RoomFailureReason.HOST_AUTH_REQUIRED),
                Triple(403, 32004, RoomFailureReason.FORBIDDEN),
                Triple(409, 32006, RoomFailureReason.FULL),
                Triple(410, 32012, RoomFailureReason.EXPIRED),
                Triple(429, 32014, RoomFailureReason.RATE_LIMITED),
            )
            cases.forEach { (status, code, reason) ->
                fixture.reply(status, """{"reasonCode":$code,"debugMessage":"fixture"}""", retryAfter = 8)
                val result = fixture.remote.create(ENTRY, UUID.randomUUID().toString(), UUID.randomUUID(), null)
                val failure = (result as RoomResult.Failed).failure
                assertEquals(reason, failure.reason)
                assertEquals(8L, failure.retryAfterSeconds)
            }
        }
    }

    @Test
    fun joinsAsGuestAndSendsExpectedVersionAtStartBoundary() = runBlocking {
        RoomHttpFixture().use { fixture ->
            val install = UUID.randomUUID().toString()
            fixture.reply(200, fixture("mobile-join-response"))
            val join = JoinAdmission("031482", "Никита", UUID.randomUUID())
            assertTrue(fixture.remote.join(join, install, null) is RoomResult.Success)
            val request = fixture.request()
            assertEquals("/api/rooms/join", request.path)
            assertEquals(join.key.toString(), request.getHeader("Idempotency-Key"))
            assertTrue(request.body.readUtf8().contains("\"code\":\"031482\""))
            assertNull(request.getHeader("Authorization"))

            fixture.reply(201, fixture("tv-create-response"))
            val host = (fixture.remote.create(ENTRY, install, UUID.randomUUID(), null) as RoomResult.Success).value.room
            fixture.request()
            fixture.reply(202, """{"roomId":"${host.id}","state":"starting","version":8}""")
            val key = UUID.randomUUID()
            assertEquals(RoomResult.Success(8L), fixture.remote.start(host, RoomCommand.Start(7, key)))
            val start = fixture.request()
            assertEquals("/api/rooms/${host.id}/start", start.path)
            assertEquals(key.toString(), start.getHeader("Idempotency-Key"))
            assertEquals("""{"expectedVersion":7}""", start.body.readUtf8())
        }
    }

    @Test
    fun joinsWithTheSameAccountThatSuppliedTheProfileName() = runBlocking {
        val credentials = CredentialsProvider { CredentialsSnapshot("fixture-access", sessionGeneration = 7) }
        RoomHttpFixture(credentials).use { fixture ->
            val installation = UUID.randomUUID().toString()
            val join = JoinAdmission("031482", "Никита", UUID.randomUUID())
            fixture.reply(200, fixture("mobile-join-response"))
            assertTrue(fixture.remote.join(join, installation, owner = 6) is RoomResult.Failed)
            assertEquals(0, fixture.requestCount)

            assertTrue(fixture.remote.join(join, installation, owner = 7) is RoomResult.Success)
            val request = fixture.request()
            assertEquals("Bearer fixture-access", request.getHeader("Authorization"))
            assertTrue(request.body.readUtf8().contains("\"displayName\":\"Никита\""))
        }
    }

    @Test
    fun createsOnlyWithTheExpectedAccountOrAnExplicitGuest() = runBlocking {
        val credentials = CredentialsProvider { CredentialsSnapshot("fixture-access", sessionGeneration = 7) }
        RoomHttpFixture(credentials).use { fixture ->
            val installation = UUID.randomUUID().toString()
            fixture.reply(201, fixture("tv-create-response"))
            assertTrue(fixture.remote.create(ENTRY, installation, UUID.randomUUID(), owner = 6) is RoomResult.Failed)
            assertEquals(0, fixture.requestCount)

            assertTrue(fixture.remote.create(ENTRY, installation, UUID.randomUUID(), owner = 7) is RoomResult.Success)
            assertEquals("Bearer fixture-access", fixture.request().getHeader("Authorization"))

            fixture.reply(201, fixture("tv-create-response"))
            val guest = fixture.remote.create(ENTRY, installation, UUID.randomUUID(), owner = null)
            assertTrue(guest is RoomResult.Success)
            assertNull(fixture.request().getHeader("Authorization"))
        }
    }

    private companion object {
        val ENTRY: UUID = UUID.fromString("78e2b5b5-05e6-43f5-9580-2e88ec713001")
    }
}
