package com.quizzgame.tv.network

import com.quizzgame.debug.network.mocks.DebugMockAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

private const val OVERSIZED_FIXTURE_BYTES = 1024 * 1024 + 1

class DebugMockAssetsTest {

    @Test
    fun fixtureReadsAndClosesItsStream() {
        var closed = false
        val assets = DebugMockAssets {
            object : ByteArrayInputStream("fixture".toByteArray()) {
                override fun close() {
                    closed = true
                    super.close()
                }
            }
        }
        assertEquals("fixture", assets.read("mock/bodies/probe.json").decodeToString())
        assertTrue(closed)
    }

    @Test
    fun fixtureExceedingSDKResponseLimitIsRejectedAndClosed() {
        var closed = false
        val assets = DebugMockAssets {
            object : ByteArrayInputStream(ByteArray(OVERSIZED_FIXTURE_BYTES)) {
                override fun close() {
                    closed = true
                    super.close()
                }
            }
        }
        assertThrows(IllegalArgumentException::class.java) { assets.read("mock/bodies/probe.json") }
        assertTrue(closed)
    }
}
