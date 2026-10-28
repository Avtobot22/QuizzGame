package com.quizzgame.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class NetworkConfigurationTest {
    @Test
    fun `production accepts HTTPS and rejects cleartext credentials and ambiguous URLs`() {
        assertEquals("https://example.com/api/", ApiBaseUrl.https("https://example.com/api/").value.toString())
        listOf(
            "http://example.com/",
            "http://127.0.0.1/",
            "https://user:password@example.com/",
            "https://example.com/?query=1",
            "https://example.com/#fragment",
            "https://example.com/api",
        ).forEach { url ->
            assertThrows(IllegalArgumentException::class.java) { ApiBaseUrl.https(url) }
        }
    }

    @Test
    fun `debug HTTP allowance is limited to literal IPv4 loopback`() {
        assertEquals("127.0.0.1", ApiBaseUrl.debugLoopback("http://127.0.0.1:12345/").value.host)
        listOf("http://localhost/", "http://0.0.0.0/", "http://192.168.1.1/", "http://example.com/").forEach { url ->
            assertThrows(IllegalArgumentException::class.java) { ApiBaseUrl.debugLoopback(url) }
        }
    }

    @Test
    fun `invalid header configuration fails before any request`() {
        assertThrows(IllegalArgumentException::class.java) { PlatformHeaders("test", "agent", 0) }
        assertThrows(IllegalArgumentException::class.java) { PlatformHeaders("", "agent", 1) }
        assertThrows(IllegalArgumentException::class.java) { PlatformHeaders("test\nunsafe", "agent", 1) }
    }

    @Test
    fun `zero negative infinite and submillisecond timeouts are rejected`() {
        listOf(Duration.ZERO, (-1).milliseconds, Duration.INFINITE, 0.5.milliseconds).forEach { timeout ->
            assertThrows(IllegalArgumentException::class.java) { NetworkTimeouts(call = timeout) }
        }
    }
}
