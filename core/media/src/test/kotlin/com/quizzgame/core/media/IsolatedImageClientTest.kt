package com.quizzgame.core.media

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.InetAddress

class IsolatedImageClientTest {
    @Test
    fun clientStripsApiHeadersAndPreservesSignedQuery() {
        var observed: Request? = null
        val client = isolatedImageClient(setOf("cdn.test")).newBuilder().addInterceptor {
            observed = it.request()
            Response.Builder().request(it.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(byteArrayOf().toResponseBody()).build()
        }.build()
        val url = "https://cdn.test/cover?signature=a%2Fb%3D&expires=123"
        client.newCall(
            Request.Builder().url(url).header("Authorization", "Bearer secret")
                .header("X-Client-Platform", "android-tv").header("X-Endpoint-Version", "1").build(),
        ).execute().close()
        assertEquals(url, observed?.url.toString())
        assertEquals(0, observed?.headers?.size)
        assertFalse(client.followRedirects)
        assertFalse(client.followSslRedirects)
    }

    @Test
    fun untrustedOriginAndNonHttpsAreRejectedBeforeTransport() {
        val client = isolatedImageClient(setOf("cdn.test"))
        listOf("https://untrusted.test/a", "http://cdn.test/a", "https://user:secret@cdn.test/a").forEach { url ->
            assertThrows(IOException::class.java) { client.newCall(Request.Builder().url(url).build()).execute() }
        }
    }

    @Test
    fun privateAndLoopbackAddressesAreRejectedIncludingIpv6AndSharedAddressSpace() {
        listOf("127.0.0.1", "10.0.0.1", "192.168.1.2", "169.254.1.1", "::1", "fc00::1", "100.64.0.1")
            .forEach { assertFalse(InetAddress.getByName(it).isPublicImageAddress()) }
        assertTrue(InetAddress.getByName("8.8.8.8").isPublicImageAddress())
    }
}
