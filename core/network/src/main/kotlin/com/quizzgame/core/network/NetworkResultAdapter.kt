package com.quizzgame.core.network

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.CancellationException

open class NetworkDiagnosticException(val diagnostic: NetworkFailure.MockConfiguration) :
    IOException("Network diagnostic: $diagnostic")

class NetworkResultAdapter(private val json: Json) {

    fun <T> execute(call: Call<T>): NetworkResult<T> {
        if (call.isCanceled) throw CancellationException("Network request was cancelled")
        return adapt(
            onIoFailure = { failure ->
                if (call.isCanceled && failure !is InterruptedIOException) {
                    throw CancellationException("Network request was cancelled")
                }
            },
        ) {
            fromResponse(call.execute())
        }
    }

    suspend fun <T> call(request: suspend () -> T): NetworkResult<T> {
        currentCoroutineContext().ensureActive()
        val result = try {
            adapt(onIoFailure = { currentCoroutineContext().ensureActive() }) {
                NetworkResult.Success(request())
            }
        } catch (failure: KotlinNullPointerException) {
            val message = failure.message.orEmpty()
            if (!message.startsWith("Response from ") ||
                !message.endsWith(" was null but response body type was declared as non-null") ||
                message.removePrefix("Response from ")
                    .removeSuffix(" was null but response body type was declared as non-null").isBlank()
            ) {
                throw failure
            }
            NetworkResult.Failure(NetworkFailure.InvalidResponse)
        }
        currentCoroutineContext().ensureActive()
        return result
    }

    suspend fun <T> response(request: suspend () -> Response<T>): NetworkResult<T> {
        currentCoroutineContext().ensureActive()
        return adapt(onIoFailure = { currentCoroutineContext().ensureActive() }) {
            fromResponse(request())
        }.also { currentCoroutineContext().ensureActive() }
    }

    private inline fun <T> adapt(
        onIoFailure: (IOException) -> Unit,
        request: () -> NetworkResult<T>,
    ): NetworkResult<T> = try {
        request()
    } catch (failure: IOException) {
        onIoFailure(failure)
        NetworkResult.Failure(fromIoException(failure))
    } catch (_: SerializationException) {
        NetworkResult.Failure(NetworkFailure.InvalidResponse)
    } catch (failure: HttpException) {
        NetworkResult.Failure(failure.response()?.let(::httpFailure) ?: NetworkFailure.Http(failure.code()))
    }

    private fun <T> fromResponse(response: Response<T>): NetworkResult<T> = if (response.isSuccessful) {
        response.body()?.let { NetworkResult.Success(it) }
            ?: NetworkResult.Failure(NetworkFailure.InvalidResponse)
    } else {
        NetworkResult.Failure(httpFailure(response))
    }

    private fun fromIoException(failure: IOException): NetworkFailure = when (failure) {
        is NetworkDiagnosticException      -> failure.diagnostic
        is UnexpectedRefreshException      -> throw failure.original
        is RefreshFailedException          -> NetworkFailure.RefreshFailed(failure.reason)
        is SessionRevokedException         -> NetworkFailure.SessionRevoked
        is AuthenticationRequiredException -> NetworkFailure.AuthenticationRequired
        is InterruptedIOException          -> NetworkFailure.Timeout
        else                               -> NetworkFailure.Connection
    }

    private fun httpFailure(response: Response<*>): NetworkFailure.Http {
        val backend = try {
            response.errorBody()?.string()?.let { json.decodeFromString<BackendErrorDetails>(it) }
        } catch (_: SerializationException) {
            null
        } catch (_: IOException) {
            null
        } finally {
            response.errorBody()?.close()
        }
        return NetworkFailure.Http(response.code(), backend)
    }
}
