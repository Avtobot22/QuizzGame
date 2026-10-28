package com.quizzgame.core.network

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestPolicyNormalizationTest {
    @Test
    fun `replay requires explicit auth policy and a bodyless GET`() {
        val request = Request.Builder().url("https://example.invalid/").build()
        assertEquals(RequestPolicy(), RequestPolicy().forRequest(request))
        assertEquals(
            RequestPolicy(),
            RequestPolicy(AuthPolicy.NONE, ReplayPolicy.AFTER_REFRESH_ONCE).forRequest(request),
        )
        listOf(AuthPolicy.OPTIONAL, AuthPolicy.REQUIRED).forEach { auth ->
            val policy = RequestPolicy(auth, ReplayPolicy.AFTER_REFRESH_ONCE)
            assertEquals(policy, policy.forRequest(request))
        }
    }

    @Test
    fun `unsafe methods and one shot or duplex POST bodies cannot enable replay`() {
        val policy = RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)
        listOf("POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS").forEach { method ->
            val body = if (method in listOf("POST", "PUT", "PATCH", "DELETE")) "fixture".toRequestBody() else null
            val request = Request.Builder().url("https://example.invalid/").method(method, body).build()
            assertEquals(ReplayPolicy.NEVER, policy.forRequest(request).replay)
        }
        listOf(false, true).forEach { duplex ->
            val body = object : RequestBody() {
                override fun contentType(): MediaType? = null
                override fun isOneShot(): Boolean = !duplex
                override fun isDuplex(): Boolean = duplex
                override fun writeTo(sink: BufferedSink) {
                    sink.writeUtf8("fixture")
                }
            }
            val request = Request.Builder().url("https://example.invalid/").post(body).build()
            assertEquals(ReplayPolicy.NEVER, policy.forRequest(request).replay)
        }
    }
}
