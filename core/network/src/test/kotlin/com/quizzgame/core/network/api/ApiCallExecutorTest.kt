package com.quizzgame.core.network.api

import com.quizzgame.core.network.result.NetworkResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class ApiCallExecutorTest {
    @Test
    fun featureRepositoryAcceptsAFakeExecutorWithoutAnApplicationOrRetrofitRuntime() = runTest {
        val fixtureService = object : FeatureApi {
            override suspend fun title(): String = "Fixture quiz"
        }
        val executor = object : ApiCallExecutor {
            override suspend fun <S : Any, T> call(service: Class<S>, request: suspend S.() -> T): ApiCallResult<T> =
                ApiCallResult.Executed(NetworkResult.Success(request(requireNotNull(service.cast(fixtureService)))))

            override suspend fun <S : Any, T> response(
                service: Class<S>,
                request: suspend S.() -> Response<T>,
            ): ApiCallResult<T> = ApiCallResult.Closed
        }
        assertEquals(ApiCallResult.Executed(NetworkResult.Success("Fixture quiz")), FeatureRepository(executor).load())
    }

    private class FeatureRepository(private val executor: ApiCallExecutor) {
        suspend fun load(): ApiCallResult<String> = executor.call(FeatureApi::class.java) { title() }
    }

    private interface FeatureApi {
        suspend fun title(): String
    }
}
