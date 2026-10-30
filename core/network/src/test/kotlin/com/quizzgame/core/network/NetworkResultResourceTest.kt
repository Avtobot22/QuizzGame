package com.quizzgame.core.network

import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkResultResourceTest {
    private val adapter = NetworkResultAdapter(createNetworkJson())

    @Test
    fun `HTTP details close the body in Response and HttpException paths even when malformed`() = runBlocking {
        listOf("{", """{"reasonCode":7,"debugMessage":"secret"}""").forEach { payload ->
            val direct = TrackedBody(payload)
            val directResult = adapter.response<Any> { Response.error(500, direct) }
            assertTrue(direct.closed)
            assertEquals(500, ((directResult as NetworkResult.Failure).reason as NetworkFailure.Http).status)
            val exception = TrackedBody(payload)
            val exceptionResult = adapter.call<Any> { throw HttpException(Response.error<Any>(500, exception)) }
            assertTrue(exception.closed)
            assertEquals(500, ((exceptionResult as NetworkResult.Failure).reason as NetworkFailure.Http).status)
        }
    }

    @Test
    fun `cancellation after an HTTP response still closes its error body`() = runBlocking {
        val body = TrackedBody("{}")
        val pending = async {
            adapter.response<Any> {
                currentCoroutineContext().cancel()
                Response.error(500, body)
            }
        }
        pending.join()
        assertTrue(pending.isCancelled)
        assertTrue(body.closed)
    }

    private class TrackedBody(payload: String) : ResponseBody() {
        var closed = false
        private val tracked = object : ForwardingSource(Buffer().writeUtf8(payload)) {
            override fun close() {
                closed = true
                super.close()
            }
        }.buffer()

        override fun contentType(): MediaType? = null
        override fun contentLength(): Long = -1
        override fun source(): BufferedSource = tracked
    }
}
