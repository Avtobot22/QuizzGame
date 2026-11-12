package com.quizzgame.core.flags

import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.api.ApiUnavailableReason
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import io.github.avtobot22.featureflags.FeatureFlagsSource
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ExecutorFeatureFlagsSourceTest {
    @Test
    fun loadUsesTheInjectedExecutorAndClosesItsResponseBody() = runTest {
        val body = ClosingBody("""{"featureToggles":[]}""")
        val executor = FakeExecutor(FlagsApi { body })
        val source = ExecutorFeatureFlagsSource(executor, FlagsApi::class.java) { flags() }
        assertEquals("""{"featureToggles":[]}""", source.loadJson())
        assertEquals(1, executor.calls)
        assertTrue(body.closed)
    }

    @Test
    fun unavailableRuntimeRetainsTypedReasonAndDoesNotUseLocalFallback() = runTest {
        val executor = FakeExecutor(FlagsApi { error("Unavailable runtime must not invoke a service") }).apply {
            unavailable = ApiUnavailableReason.CONFIGURATION
        }
        var localReads = 0
        val adapter = FeatureFlagsSdkAdapter(
            ExecutorFeatureFlagsSource(executor, FlagsApi::class.java) { flags() },
            FeatureFlagsSource {
                localReads++
                """{"featureToggles":[]}"""
            },
            AppFeatureDefinitions.tvGuest,
        )
        assertEquals(
            FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.Runtime(ApiUnavailableReason.CONFIGURATION)),
            adapter.refresh(),
        )
        assertEquals(0, localReads)
        assertFalse(adapter.guestAllowed())
    }

    @Test
    fun executorNetworkFailureIsPreservedWithoutRawExceptionText() = runTest {
        val executor = FakeExecutor(FlagsApi { error("No request expected") }).apply {
            failure = NetworkFailure.Timeout
        }
        val adapter = FeatureFlagsSdkAdapter(
            ExecutorFeatureFlagsSource(executor, FlagsApi::class.java) { flags() },
            FeatureFlagsSource { error("No local fallback on timeout") },
            AppFeatureDefinitions.tvGuest,
        )
        assertEquals(
            FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.Network(NetworkFailure.Timeout)),
            adapter.refresh(),
        )
    }

    private fun interface FlagsApi {
        suspend fun flags(): ResponseBody
    }

    private class FakeExecutor(private val api: FlagsApi) : ApiCallExecutor {
        var calls = 0
        var unavailable: ApiUnavailableReason? = null
        var failure: NetworkFailure? = null

        override suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T> {
            calls++
            val missing = unavailable
            val failed = failure
            return when {
                missing != null -> ApiCallResult.Unavailable(missing)
                failed != null -> ApiCallResult.Executed(NetworkResult.Failure(failed))
                else -> ApiCallResult.Executed(NetworkResult.Success(request(requireNotNull(service.cast(api)))))
            }
        }

        override suspend fun <S : Any, T> response(
            service: Class<S>,
            request: suspend S.() -> Response<T>,
        ): ApiCallResult<T> = error("Unexpected response call")
    }

    private class ClosingBody(payload: String) : ResponseBody() {
        var closed = false
        private val buffer = Buffer().writeUtf8(payload)
        private val stream = object : ForwardingSource(buffer) {
            override fun close() {
                closed = true
                super.close()
            }
        }.buffer()

        override fun contentType(): MediaType? = null
        override fun contentLength(): Long = -1
        override fun source(): BufferedSource = stream
    }
}
