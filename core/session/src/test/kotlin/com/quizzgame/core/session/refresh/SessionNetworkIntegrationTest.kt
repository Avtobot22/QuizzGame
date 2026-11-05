package com.quizzgame.core.session.refresh

import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.AuthenticationRequiredException
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.client.NetworkClientFactory
import com.quizzgame.core.network.config.ApiBaseUrl
import com.quizzgame.core.network.config.NetworkConfiguration
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.DataStoreSessionStore
import com.quizzgame.core.session.storage.testAead
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Tag
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class SessionNetworkIntegrationTest {

    @get:Rule
    val temporary = TemporaryFolder()

    @Test
    fun `restored secure storage is visible to HTTP and logout blocks another protected request`() = runTest {
        val file = File(temporary.root, "session.enc")
        val store = DataStoreSessionStore(file, testAead())
        store.write(TokenPair("stored-access", "stored-refresh"))
        val manager = SessionManager.open(store)
        val server = MockWebServer()
        server.start(InetAddress.getByName("127.0.0.1"), 0)
        val client = NetworkClientFactory(
            NetworkConfiguration(
                ApiBaseUrl.debugLoopback(server.url("/").newBuilder().host("127.0.0.1").build().toString()),
                PlatformHeaders("test-installation", "session-tests/1"),
            ),
            createNetworkJson(),
            manager,
        ).createApiClient()
        try {
            val api = client.retrofit.create(ProtectedApi::class.java)
            val policy = RequestPolicy(AuthPolicy.REQUIRED)
            server.enqueue(MockResponse().setBody("ok"))
            api.get(policy).execute().body()!!.close()
            assertEquals("Bearer stored-access", server.takeRequest(1, TimeUnit.SECONDS)!!.getHeader("Authorization"))
            manager.logout()
            assertThrows(AuthenticationRequiredException::class.java) { api.get(policy).execute() }
            assertEquals(1, server.requestCount)
        } finally {
            manager.close()
            client.httpClient.connectionPool.evictAll()
            client.httpClient.dispatcher.executorService.shutdownNow()
            server.shutdown()
        }
    }

    interface ProtectedApi {

        @GET("protected")
        fun get(@Tag policy: RequestPolicy): Call<ResponseBody>
    }
}
