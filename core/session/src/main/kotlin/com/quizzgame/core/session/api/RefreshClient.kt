package com.quizzgame.core.session.api

import com.quizzgame.core.session.TokenPair
import com.quizzgame.core.session.api.dto.RefreshRequestDto
import com.quizzgame.core.session.api.dto.RefreshResponseDto
import kotlinx.serialization.SerializationException
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean

class RefreshClient(retrofit: Retrofit, private val endpoint: RefreshEndpoint) {

    private val api = retrofit.create(RefreshApi::class.java)

    fun newCall(refreshToken: String, deviceId: String): RefreshCall =
        RefreshCall(api.refresh(endpoint.path, RefreshRequestDto(refreshToken, deviceId)))
}

class RefreshCall internal constructor(private val call: Call<RefreshResponseDto>) {
    private val explicitlyCancelled = AtomicBoolean()

    fun cancel() {
        explicitlyCancelled.set(true)
        call.cancel()
    }

    fun execute(): TokenPair {
        checkCancellation()
        try {
            return tokensFrom(call.execute())
        } catch (_: SerializationException) {
            fail(RefreshFailure.InvalidResponse)
        } catch (_: IllegalArgumentException) {
            fail(RefreshFailure.InvalidResponse)
        } catch (failure: RefreshException) {
            throw failure
        } catch (_: InterruptedIOException) {
            timeout()
        } catch (_: IOException) {
            fail(RefreshFailure.Io)
        }
    }

    private fun tokensFrom(response: Response<RefreshResponseDto>): TokenPair = try {
        checkCancellation()
        if (!response.isSuccessful) fail(RefreshFailure.Http(response.code()))
        response.body()?.toTokenPair() ?: fail(RefreshFailure.InvalidResponse)
    } finally {
        response.errorBody()?.close()
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

    private fun timeout(): Nothing {
        if (explicitlyCancelled.get() || Thread.currentThread().isInterrupted) {
            throw CancellationException("Refresh request was cancelled")
        }
        throw RefreshException(RefreshFailure.Timeout)
    }
}
