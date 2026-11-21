package com.quizzgame.shared.room.data

import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.shared.room.data.dto.admission.CreateRoomResponse
import com.quizzgame.shared.room.data.dto.admission.JoinRoomResponse
import com.quizzgame.shared.room.data.dto.snapshot.SnapshotDto
import com.quizzgame.shared.room.data.mapper.toDomain
import com.quizzgame.shared.room.data.stream.RoomStreamDecoder
import com.quizzgame.shared.room.data.stream.RoomStreamEvent
import com.quizzgame.shared.room.domain.entity.admission.RoomEndpointSettings
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinInput
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import com.quizzgame.shared.room.domain.entity.result.RoomFailureReason
import com.quizzgame.shared.room.domain.entity.session.RoomRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RoomContractFixturesTest {
    private val json = createNetworkJson()
    private val links = RoomJoinLink(RoomEndpointSettings("join.example.test"))
    private val decoder = RoomStreamDecoder(json, links)

    @Test
    fun hostAndParticipantFixturesDecodeWithSeparateCapabilities() {
        val host = json.decodeFromString<CreateRoomResponse>(fixture("tv-create-response"))
        val player = json.decodeFromString<JoinRoomResponse>(fixture("mobile-join-response"))
        val hostSnapshot = host.room.toDomain(RoomRole.HOST, links)
        val playerSnapshot = player.room.toDomain(RoomRole.PARTICIPANT, links)
        assertEquals(hostSnapshot.roomId, playerSnapshot.roomId)
        assertEquals("031482", hostSnapshot.join?.code)
        assertEquals("https://join.example.test/r/031482", hostSnapshot.join?.url)
        assertNull(playerSnapshot.join)
        assertEquals(RoomRole.HOST, host.credential.toDomain().role)
        assertEquals(RoomRole.PARTICIPANT, player.credential.toDomain().role)
        assertEquals(player.participant.toDomain(), playerSnapshot.players.single())
        assertFalse(host.toString().contains(host.credential.roomToken))
        assertFalse(player.credential.toDomain().toString().contains(player.credential.roomToken))
        assertEquals(hostSnapshot, snapshot("tv-room-snapshot").toDomain(RoomRole.HOST, links))
        assertEquals(playerSnapshot, snapshot("mobile-room-snapshot").toDomain(RoomRole.PARTICIPANT, links))
    }

    @Test
    fun realtimeFixturesDecodeAndValidateEnvelopeVersion() {
        val frame = fixture("ws-room-snapshot")
        val event = decoder.decode(frame, RoomRole.PARTICIPANT) as RoomStreamEvent.Snapshot
        assertEquals(2, event.value.version.toInt())
        val closed = decoder.decode(fixture("ws-room-closed"), RoomRole.PARTICIPANT)
        assertEquals(RoomStreamEvent.Ended(3, RoomFailureReason.CLOSED), closed)
        val started = decoder.decode(fixture("ws-room-started"), RoomRole.HOST)
        val gameId = UUID.fromString("82ca03be-48cc-4ad5-a62e-cb184860aa11")
        assertEquals(RoomStreamEvent.Started(4, gameId), started)
        assertNull(decoder.decode(frame.replace("room.snapshot", "room.future"), RoomRole.PARTICIPANT))
        val wrongVersion = frame.replaceFirst("\"version\": 2", "\"version\": 9")
        assertEquals(RoomStreamEvent.Invalid, decoder.decode(wrongVersion, RoomRole.PARTICIPANT))
    }

    @Test
    fun qrRequiresExactOriginAndExactCode() {
        assertEquals("031482", links.codeFrom("https://join.example.test/r/031482"))
        assertNull(links.codeFrom("https://join.example.test/r/031482?token=secret"))
        assertNull(links.codeFrom("https://join.example.test.evil.invalid/r/031482"))
        assertNull(links.codeFrom("https://user@join.example.test/r/031482"))
        assertNull(links.codeFrom("https://join.example.test:8443/r/031482"))
        assertNull(links.codeFrom("https://join.example.test/r/%3031482"))
        assertNull(links.codeFrom("http://join.example.test/r/031482"))
        val snapshot = snapshot("tv-room-snapshot")
        val malicious = snapshot.copy(join = snapshot.join?.copy(url = "https://join.example.test/r/012345"))
        assertNull(malicious.toDomain(RoomRole.HOST, links).join?.url)
    }

    @Test
    fun normalizesNicknameAndValidatesPin() {
        assertEquals("Café", RoomJoinInput.nickname(" Café "))
        assertNull(RoomJoinInput.nickname("a".repeat(25)))
        assertTrue(RoomJoinInput.validCode("000001"))
        assertFalse(RoomJoinInput.validCode("123"))
    }

    private fun snapshot(name: String): SnapshotDto = json.decodeFromString(fixture(name))
}

internal fun fixture(name: String): String =
    checkNotNull(RoomContractFixturesTest::class.java.getResource("/rooms/$name.json")).readText()
