package com.quizzgame.core.session.api

import com.quizzgame.core.network.NetworkResult
import com.quizzgame.core.network.NetworkResultAdapter
import com.quizzgame.core.session.TokenPair
import com.quizzgame.core.session.api.dto.RefreshRequestDto
import com.quizzgame.core.session.api.dto.RefreshResponseDto
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

    fun cancel() {
        explicitlyCancelled.set(true)
        call.cancel()
    }

    fun execute(): TokenPair {
        checkCancellation()
        return when (val result = results.execute(call)) {
            is NetworkResult.Success -> try {
                checkCancellation()
                result.value.toTokenPair()
            } catch (_: IllegalArgumentException) {
                fail(RefreshFailure.InvalidResponse)
            }
            is NetworkResult.Failure -> {
                val failure = result.reason.toRefreshFailure(
                    explicitlyCancelled.get() || Thread.currentThread().isInterrupted,
                )
                if (failure == RefreshFailure.Timeout) throw RefreshException(failure)
                fail(failure)
            }
        }
    }

    private fun fail(failure: RefreshFailure): Nothing {
        checkCancellation()
        throw RefreshException(failure)
    }

    private fun checkCancellation() {
        if (call.isCanceled || Thread.currentThread().isInterrupted) {
            throw CancellationException("Refresh request was cancelled")
        }
    }
}
