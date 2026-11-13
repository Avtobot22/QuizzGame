package com.quizzgame.shared.splash.data

import com.quizzgame.core.flags.AppFeatureDefinitions
import com.quizzgame.core.flags.ExecutorFeatureFlagsSource
import com.quizzgame.core.flags.FeatureFlagsSdkAdapter
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.auth.AuthPolicy
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.auth.ReplayPolicy
import com.quizzgame.core.network.auth.RequestPolicy
import com.quizzgame.core.network.client.NetworkClient
import com.quizzgame.core.network.client.NetworkClientFactory
import com.quizzgame.core.network.config.ApiBaseUrl
import com.quizzgame.core.network.config.NetworkConfiguration
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.refresh.ContractRefreshRevocationPolicy
import com.quizzgame.core.session.refresh.SessionRefreshCoordinator
import com.quizzgame.core.session.refresh.api.RefreshClient
import com.quizzgame.core.session.refresh.api.RefreshEndpoint
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStore
import com.quizzgame.shared.splash.data.dto.LaunchDto
import com.quizzgame.shared.splash.data.dto.UserDto
import com.quizzgame.shared.splash.data.mapper.toDomain
import com.quizzgame.shared.splash.data.mapper.validated
import com.quizzgame.shared.splash.domain.StartSplashUseCase
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import com.quizzgame.shared.splash.domain.entity.SplashOutcome
import io.github.avtobot22.featureflags.FeatureFlagsSource
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Tag
import java.io.Closeable
import java.net.InetAddress

class SplashSessionHttpTest {
    @Test
    fun userUnauthorizedRefreshesAndReplaysOnceBeforePublishingCurrentUser() = runTest {
        Fixture.open().use { fixture ->
            fixture.enqueueBootstrap()
            fixture.server.enqueue(MockResponse().setResponseCode(401))
            fixture.server.enqueue(MockResponse().setBody(ROTATED))
            fixture.server.enqueue(MockResponse().setBody(USER))
            assertEquals(SplashOutcome.Continue, fixture.useCase())
            assertEquals(5, fixture.server.requestCount)
            assertEquals("fixture-access-2", fixture.store.record.tokens?.access)
            assertEquals("fixture-refresh-2", fixture.store.record.tokens?.refresh)
            assertTrue(fixture.users.current() != null)
            val requests = List(5) { fixture.server.takeRequest() }
            assertEquals(
                listOf(
                    "/api/tv/launch",
                    "/api/tv/feature-toggles",
                    "/api/tv/user",
                    "/api/tv/auth/refresh",
                    "/api/tv/user",
                ),
                requests.map { it.path },
            )
            assertEquals("Bearer fixture-access-1", requests[2].getHeader("Authorization"))
            assertNull(requests[3].getHeader("Authorization"))
            assertEquals("Bearer fixture-access-2", requests[4].getHeader("Authorization"))
        }
    }

    @Test
    fun secondUserUnauthorizedStopsWithoutAnotherRefreshOrNavigation() = runTest {
        Fixture.open().use { fixture ->
            fixture.enqueueBootstrap()
            fixture.server.enqueue(MockResponse().setResponseCode(401))
            fixture.server.enqueue(MockResponse().setBody(ROTATED))
            fixture.server.enqueue(MockResponse().setResponseCode(401))
            assertFalse(fixture.useCase() is SplashOutcome.Continue)
            assertEquals(5, fixture.server.requestCount)
            assertNull(fixture.users.current())
        }
    }

    @Test
    fun confirmedRefreshRevocationExpiresSessionAndRoutesToSignIn() = runTest {
        Fixture.open().use { fixture ->
            fixture.enqueueBootstrap()
            fixture.server.enqueue(MockResponse().setResponseCode(401))
            fixture.server.enqueue(
                MockResponse().setResponseCode(400)
                    .setBody("""{"reasonCode":21001,"debugMessage":"invalid","userFriendlyMessage":null}"""),
            )
            assertEquals(SplashOutcome.SignIn, fixture.useCase())
            assertEquals(SessionState.Expired, fixture.session.state.value)
            assertEquals(4, fixture.server.requestCount)
            assertNull(fixture.store.record.tokens)
            assertNull(fixture.users.current())
        }
    }

    @Test
    fun refreshServerFailureBlocksOldPairWithoutLogoutOrBlindReplay() = runTest {
        Fixture.open().use { fixture ->
            fixture.enqueueBootstrap()
            fixture.server.enqueue(MockResponse().setResponseCode(401))
            fixture.server.enqueue(MockResponse().setResponseCode(503))
            assertEquals(SplashOutcome.SignIn, fixture.useCase())
            val state = fixture.session.state.value as SessionState.Blocked
            assertEquals(CredentialUnavailableKind.REFRESH_UNCERTAIN, state.kind)
            assertEquals("fixture-refresh-1", fixture.store.record.tokens?.refresh)
            assertEquals(4, fixture.server.requestCount)
            assertNull(fixture.users.current())
            assertEquals(SplashOutcome.SignIn, fixture.useCase())
            assertEquals(4, fixture.server.requestCount)
        }
    }

    private class Fixture(
        val server: MockWebServer,
        val store: Store,
        val session: SessionManager,
        private val refresh: NetworkClient,
        private val api: NetworkClient,
        private val coordinator: SessionRefreshCoordinator,
    ) : Closeable {
        private val executor = object : ApiCallExecutor {
            override suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T> =
                ApiCallResult.Executed(api.results.call { request(api.retrofit.create(service)) })

            override suspend fun <S : Any, T> response(
                service: Class<S>,
                request: suspend S.() -> Response<T>,
            ): ApiCallResult<T> = ApiCallResult.Executed(api.results.response { request(api.retrofit.create(service)) })
        }
        private val flags = FeatureFlagsSdkAdapter(
            ExecutorFeatureFlagsSource(executor, FlagsApi::class.java) {
                flags(RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE))
            },
            FeatureFlagsSource { """{"featureToggles":[]}""" },
            AppFeatureDefinitions.tvGuest,
        )
        private val reader = SessionStateReader { session.state.value }
        val users = CurrentUserStore(reader)
        private val remote = object : SplashRemoteDataSource {
            override suspend fun launch(): ApiCallResult<Unit> = executor.call(TestSplashApi::class.java) {
                launch(RequestPolicy(AuthPolicy.OPTIONAL, ReplayPolicy.AFTER_REFRESH_ONCE)).validated()
                Unit
            }
            override suspend fun currentUser(): ApiCallResult<CurrentUser> = executor.call(TestSplashApi::class.java) {
                currentUser(RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE)).toDomain()
            }
        }
        val useCase = StartSplashUseCase(
            SplashRepositoryImpl(
                remote,
                flags,
                flags,
                reader,
                users,
            ),
        )

        fun enqueueBootstrap() {
            server.enqueue(MockResponse().setBody("""{"id":"61d779f2-e9cd-42f0-ae96-f69a6ae2926f"}"""))
            server.enqueue(MockResponse().setBody("""{"featureToggles":[]}"""))
        }

        override fun close() {
            coordinator.close()
            listOf(api, refresh).forEach { client ->
                client.httpClient.dispatcher.cancelAll()
                client.httpClient.connectionPool.evictAll()
                client.httpClient.dispatcher.executorService.shutdownNow()
            }
            session.close()
            server.shutdown()
        }

        companion object {
            suspend fun open(): Fixture {
                val server = MockWebServer().apply { start(InetAddress.getByName("127.0.0.1"), 0) }
                val store = Store()
                val session = SessionManager.open(store)
                val configuration = NetworkConfiguration(
                    ApiBaseUrl.debugLoopback(server.url("/").newBuilder().host("127.0.0.1").build().toString()),
                    PlatformHeaders("android-tv", "SplashFixture/1"),
                )
                val json = createNetworkJson()
                val refresh = NetworkClientFactory(configuration, json).createRefreshClient()
                val coordinator = SessionRefreshCoordinator(
                    session,
                    RefreshClient(refresh.retrofit, RefreshEndpoint("api/tv/auth/refresh"), refresh.results),
                    refresh.httpClient.dispatcher.executorService,
                    ContractRefreshRevocationPolicy(),
                )
                val api = NetworkClientFactory(configuration, json, session, coordinator).createApiClient()
                return Fixture(server, store, session, refresh, api, coordinator)
            }
        }
    }

    private class Store : SessionStore {
        var record = SessionRecord(
            "e4a508be-bb66-46d3-91f6-7b4cabfc977b",
            TokenPair("fixture-access-1", "fixture-refresh-1"),
        )
        override fun read(): SessionRecord = record
        override fun write(tokens: TokenPair?): SessionRecord = writeRecord(SessionRecord(record.deviceId, tokens))
        override fun writeRecord(record: SessionRecord): SessionRecord = record.also { this.record = it }
    }

    private interface TestSplashApi {
        @GET("api/tv/launch")
        suspend fun launch(@Tag policy: RequestPolicy): LaunchDto

        @GET("api/tv/user")
        suspend fun currentUser(@Tag policy: RequestPolicy): UserDto
    }

    private interface FlagsApi {
        @GET("api/tv/feature-toggles")
        suspend fun flags(@Tag policy: RequestPolicy): ResponseBody
    }

    private companion object {
        const val ROTATED = """{"access":"fixture-access-2","refresh":"fixture-refresh-2"}"""
        const val USER = """{"id":"719278b4-e7af-4e50-af40-25701f2b2b27",""" +
            """"account":{"createdAt":"2026-11-05T10:30:00Z"},"profile":{},"roles":[]}"""
    }
}
