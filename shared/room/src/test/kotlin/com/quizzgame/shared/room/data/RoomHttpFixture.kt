package com.quizzgame.shared.room.data

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.CredentialsProvider
import com.quizzgame.core.network.client.NetworkClient
import com.quizzgame.core.network.client.NetworkClientFactory
import com.quizzgame.core.network.config.ApiBaseUrl
import com.quizzgame.core.network.config.NetworkConfiguration
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.shared.room.data.api.RoomRemoteDataSource
import com.quizzgame.shared.room.domain.entity.admission.RoomEndpointSettings
import com.quizzgame.shared.room.domain.entity.admission.RoomJoinLink
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Response
import java.io.Closeable

internal class RoomHttpFixture(credentials: CredentialsProvider = CredentialsProvider.guest()) : Closeable {
    private val server = MockWebServer().apply { start() }
    val json = createNetworkJson()
    val links = RoomJoinLink(RoomEndpointSettings("join.example.test"))
    val client: NetworkClient = NetworkClientFactory(
        NetworkConfiguration(
            ApiBaseUrl.debugLoopback("http://127.0.0.1:${server.port}/"),
            PlatformHeaders("android-tv", "Rooms-Test"),
        ),
        json,
        credentials,
    ).createApiClient()
    val remote = RoomRemoteDataSource(
        object : ApiCallExecutor {
            override suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T> =
                ApiCallResult.Executed(client.results.call { client.retrofit.create(service).request() })

            override suspend fun <S : Any, T> response(
                service: Class<S>,
                request: suspend S.() -> Response<T>,
            ): ApiCallResult<T> =
                ApiCallResult.Executed(client.results.response { client.retrofit.create(service).request() })
        },
        links,
    )

    fun reply(status: Int, body: String = "", retryAfter: Long? = null) {
        server.enqueue(
            MockResponse().setResponseCode(status).setBody(body).apply {
                retryAfter?.let { setHeader("Retry-After", it) }
            },
        )
    }

    fun enqueue(response: MockResponse) = server.enqueue(response)

    val requestCount get() = server.requestCount

    fun request() = server.takeRequest()

    override fun close() {
        client.httpClient.dispatcher.cancelAll()
        client.httpClient.connectionPool.evictAll()
        server.shutdown()
    }
}
