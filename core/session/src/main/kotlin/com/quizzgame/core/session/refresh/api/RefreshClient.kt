package com.quizzgame.core.session.refresh.api

import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.network.result.NetworkResultAdapter
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.refresh.api.dto.RefreshRequestDto
import com.quizzgame.core.session.refresh.api.dto.RefreshResponseDto
import retrofit2.Call
import retrofit2.Retrofit
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean

class RefreshClient(
    retrofit: Retrofit,
    private val endpoint: RefreshEndpoint,
    private val results: NetworkResultAdapter,
) {
    private val api = retrofit.create(RefreshApi::class.java)

    fun newCall(refreshToken: String, deviceId: String): RefreshCall =
        RefreshCall(api.refresh(endpoint.path, RefreshRequestDto(refreshToken, deviceId)), results)
}

class RefreshCall internal constructor(
    private val call: Call<RefreshResponseDto>,
    private val results: NetworkResultAdapter,
) {
    private val explicitlyCancelled = AtomicBoolean()
    internal val hasExecuted: Boolean get() = call.isExecuted

    fun cancel() {
        explicitlyCancelled.set(true)
        call.cancel()
    }

    fun execute(): TokenPair {
        if (explicitlyCancelled.get() || Thread.currentThread().isInterrupted) {
            throw CancellationException("Refresh request was cancelled")
        }
        return when (val result = results.execute(call)) {
            is NetworkResult.Success -> result.value.validatedTokens()
            is NetworkResult.Failure -> throw RefreshException(
                result.reason.toRefreshFailure(explicitlyCancelled.get() || Thread.currentThread().isInterrupted),
            )
        }
    }
}

private fun RefreshResponseDto.validatedTokens(): TokenPair = try {
    // Once B is received, persist it even when the last waiter has just cancelled.
    toTokenPair()
} catch (_: IllegalArgumentException) {
    throw RefreshException(RefreshFailure.InvalidResponse)
}
