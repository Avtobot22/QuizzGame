package com.quizzgame.mobile.network

import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.CredentialsProvider
import com.quizzgame.core.network.CredentialsSnapshot
import com.quizzgame.core.network.PlatformHeaders
import com.quizzgame.core.network.RequestPolicy
import com.quizzgame.debug.network.DebugMockFailure
import com.quizzgame.debug.network.DebugMockTransportException
import com.quizzgame.debug.network.DebugNetworkConfiguration
import com.quizzgame.debug.network.DebugNetworkGraph
import com.quizzgame.debug.network.DebugNetworkMode
import com.quizzgame.debug.network.mocks.DebugMockAssets
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Tag
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class MobileDebugNetworkTest {
    private val graphs = mutableListOf<DebugNetworkGraph>()

    @After
    fun closeGraphs() {
        graphs.forEach(DebugNetworkGraph::close)
    }

    @Test
    fun bothClientsUseMobileFixtureThroughTheSameLoopbackServer() {
        val graph = openMock()
        assertEquals("127.0.0.1", graph.api.retrofit.baseUrl().host)
        assertEquals("http", graph.api.retrofit.baseUrl().scheme)
        assertTrue(graph.api.retrofit.baseUrl().port > 0)
        assertEquals(graph.api.retrofit.baseUrl(), graph.refresh.retrofit.baseUrl())
        assertNotSame(graph.api.httpClient.connectionPool, graph.refresh.httpClient.connectionPool)
        listOf(graph.api, graph.refresh).forEach { client ->
            val response = client.retrofit.create(MobileProbeApi::class.java).probe().execute()
            assertEquals("mobile-debug-transport", response.body()!!["source"]!!.jsonPrimitive.content)
        }
    }

    @Test
    fun unconfiguredMobileRequestReturnsTypedMockFailure() {
        val graph = openMock()
        val api = graph.api.retrofit.create(MobileProbeApi::class.java)
        val failure = assertThrows(DebugMockTransportException::class.java) { api.missing().execute() }
        assertEquals(DebugMockFailure.NO_MATCHING_RULE, failure.failure)
    }

    @Test
    fun separateGraphsKeepIndependentRulesAndClosingOneLeavesTheOtherUsable() {
        val firstCredentials = AtomicReference(CredentialsSnapshot("first-access", 1))
        val first = openMock("""{"source":"first-graph"}""", CredentialsProvider { firstCredentials.get() })
        val second = openMock(
            """{"source":"second-graph"}""",
            CredentialsProvider { CredentialsSnapshot("second-access", 1) },
        )
        assertNotEquals(first.api.retrofit.baseUrl(), second.api.retrofit.baseUrl())
        assertNotSame(first.api.httpClient.connectionPool, second.api.httpClient.connectionPool)
        assertNotSame(first.refresh.httpClient.dispatcher, second.refresh.httpClient.dispatcher)
        val firstApi = first.api.retrofit.create(MobileProbeApi::class.java)
        val secondApi = second.api.retrofit.create(MobileProbeApi::class.java)
        val policy = RequestPolicy(AuthPolicy.OPTIONAL)
        val firstResponse = firstApi.probe(policy).execute()
        val secondResponse = secondApi.probe(policy).execute()
        assertEquals("first-graph", firstResponse.body()!!["source"]!!.jsonPrimitive.content)
        assertEquals("second-graph", secondResponse.body()!!["source"]!!.jsonPrimitive.content)
        assertEquals("Bearer first-access", firstResponse.raw().request.header("Authorization"))
        assertEquals("Bearer second-access", secondResponse.raw().request.header("Authorization"))
        firstCredentials.set(CredentialsSnapshot("changed-first-access", 2))
        val updated = firstApi.probe(policy).execute()
        assertEquals("Bearer changed-first-access", updated.raw().request.header("Authorization"))
        first.close()
        assertThrows(IOException::class.java) { firstApi.probe().execute() }
        val remaining = secondApi.probe(policy).execute()
        assertEquals("second-graph", remaining.body()!!["source"]!!.jsonPrimitive.content)
        assertEquals("Bearer second-access", remaining.raw().request.header("Authorization"))
    }

    private fun openMock(
        fixture: String? = null,
        credentialsProvider: CredentialsProvider = CredentialsProvider.guest(),
    ): DebugNetworkGraph = openMobileDebugNetwork(
        DebugNetworkConfiguration(
            DebugNetworkMode.MOCK,
            PlatformHeaders("test-mobile", "mobile-debug-tests/1", 1),
        ),
        bodyAssets = DebugMockAssets { path ->
            fixture?.let { ByteArrayInputStream(it.toByteArray()) }
                ?: checkNotNull(javaClass.classLoader!!.getResourceAsStream(path))
        },
        credentialsProvider = credentialsProvider,
    ).also(graphs::add)

    interface MobileProbeApi {
        @GET("debug/mobile/probe")
        fun probe(@Tag policy: RequestPolicy? = null): Call<JsonObject>

        @GET("debug/mobile/missing")
        fun missing(): Call<JsonObject>
    }
}
