package com.quizzgame.core.session

import com.quizzgame.core.network.ApiBaseUrl
import com.quizzgame.core.network.AuthPolicy
import com.quizzgame.core.network.NetworkClient
import com.quizzgame.core.network.NetworkClientFactory
import com.quizzgame.core.network.NetworkConfiguration
import com.quizzgame.core.network.NetworkTimeouts
import com.quizzgame.core.network.PlatformHeaders
import com.quizzgame.core.network.ReplayPolicy
import com.quizzgame.core.network.RequestPolicy
import com.quizzgame.core.session.api.RefreshClient
import com.quizzgame.core.session.api.RefreshEndpoint
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.CancellationException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

internal const val RECOVERY_ACCESS_1 = "fixture-recovery-access-1"
internal const val RECOVERY_ACCESS_2 = "fixture-recovery-access-2"
internal const val RECOVERY_REFRESH_1 = "fixture-recovery-refresh-1"
internal const val RECOVERY_REFRESH_2 = "fixture-recovery-refresh-2"
internal const val RECOVERY_REFRESH_PATH = "/auth/refresh"
internal const val RECOVERY_WAIT_SECONDS = 5L

abstract class SessionRecoveryTestFixture {

    private val fixtures = mutableListOf<RecoveryFixture>()
    private val calls = CopyOnWriteArrayList<Call>()
    private val gates = mutableListOf<CountDownLatch>()
    private val executor = Executors.newCachedThreadPool()

    @After
    fun closeRecoveryFixtures() {
        gates.forEach(CountDownLatch::countDown)
        calls.forEach(Call::cancel)
        fixtures.asReversed().forEach { fixture ->
            fixture.coordinator.close()
            closeNetwork(fixture.api)
            closeNetwork(fixture.refresh)
            fixture.manager.close()
            fixture.server.shutdown()
            listOf(fixture.api, fixture.refresh).forEach { network ->
                val worker = network.httpClient.dispatcher.executorService
                check(worker.awaitTermination(RECOVERY_WAIT_SECONDS, TimeUnit.SECONDS))
            }
        }
        executor.shutdownNow()
        check(executor.awaitTermination(RECOVERY_WAIT_SECONDS, TimeUnit.SECONDS))
    }

    protected fun gate(): CountDownLatch = CountDownLatch(1).also(gates::add)

    protected fun await(gate: CountDownLatch) {
        check(gate.await(RECOVERY_WAIT_SECONDS, TimeUnit.SECONDS)) { "Recovery test synchronization timed out" }
    }

    protected fun openRecovery(
        dispatcher: Dispatcher,
        revocationPolicy: RefreshRevocationPolicy = RefreshRevocationPolicy.never(),
        timeouts: NetworkTimeouts = NetworkTimeouts(
            connect = 2.seconds,
            read = 5.seconds,
            write = 2.seconds,
            call = 8.seconds,
        ),
        storeDecorator: (SessionStore) -> SessionStore = { it },
    ): RecoveryFixture {
        val server = MockWebServer().apply {
            this.dispatcher = dispatcher
            start(InetAddress.getByName("127.0.0.1"), 0)
        }
        val store = FakeSessionStore(TokenPair(RECOVERY_ACCESS_1, RECOVERY_REFRESH_1))
        val manager = runBlocking { SessionManager.open(storeDecorator(store)) }
        val configuration = NetworkConfiguration(
            ApiBaseUrl.debugLoopback(server.url("/").newBuilder().host("127.0.0.1").build().toString()),
            PlatformHeaders("recovery-test", "recovery-tests/1", 1),
            timeouts,
        )
        val refresh = NetworkClientFactory(configuration, manager).createRefreshClient()
        val coordinator = SessionRefreshCoordinator(
            manager,
            RefreshClient(refresh.retrofit, RefreshEndpoint(RECOVERY_REFRESH_PATH.removePrefix("/"))),
            refresh.httpClient.dispatcher.executorService,
            revocationPolicy,
        )
        val api = NetworkClientFactory(configuration, manager, coordinator).createApiClient()
        return RecoveryFixture(server, store, manager, coordinator, api, refresh).also(fixtures::add)
    }

    protected fun newCall(fixture: RecoveryFixture, path: String): Call = fixture.api.httpClient.newCall(
        Request.Builder().url(checkNotNull(fixture.api.retrofit.baseUrl().resolve(path)))
            .tag(RequestPolicy::class.java, RequestPolicy(AuthPolicy.REQUIRED, ReplayPolicy.AFTER_REFRESH_ONCE))
            .build(),
    ).also(calls::add)

    protected fun executeAsync(call: Call): Future<HttpResult> = executor.submit<HttpResult> { execute(call) }

    protected fun <T> onCaller(action: () -> T): Future<T> = executor.submit<T> { action() }

    protected fun execute(call: Call): HttpResult = try {
        call.execute().use { HttpResult(it.code, null) }
    } catch (failure: IOException) {
        HttpResult(null, failure)
    } catch (cancelled: CancellationException) {
        HttpResult(null, cancelled)
    }

    protected fun <T> result(pending: Future<T>): T =
        pending.get(RECOVERY_WAIT_SECONDS, TimeUnit.SECONDS)

    protected fun pairResponse(access: String = RECOVERY_ACCESS_2): MockResponse = MockResponse().setBody(
        """{"access":"$access","refresh":"$RECOVERY_REFRESH_2"}""",
    )

    protected class RecoveryFixture internal constructor(
        val server: MockWebServer,
        internal val store: FakeSessionStore,
        val manager: SessionManager,
        val coordinator: SessionRefreshCoordinator,
        val api: NetworkClient,
        val refresh: NetworkClient,
    )

    protected class HttpResult(val status: Int?, val failure: Exception?)
}

private fun closeNetwork(network: NetworkClient) {
    network.httpClient.dispatcher.cancelAll()
    network.httpClient.connectionPool.evictAll()
    network.httpClient.dispatcher.executorService.shutdown()
}
