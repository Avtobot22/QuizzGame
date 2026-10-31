package com.quizzgame.core.error

import com.quizzgame.core.network.BackendErrorDetails
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.RefreshFailureReason
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshErrorClassifierTest {

    private val classifier: ErrorClassifier = DefaultErrorClassifier()

    @Test
    fun `refresh IO and timeout keep connectivity cause`() {
        assertEquals(
            AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE),
            classify(RefreshFailureReason.Io),
        )
        assertEquals(AppFailure.Connectivity(ConnectivityKind.TIMEOUT), classify(RefreshFailureReason.Timeout))
    }

    @Test
    fun `refresh invalid response and persistence failure remain distinct`() {
        assertEquals(AppFailure.InvalidServerData, classify(RefreshFailureReason.InvalidResponse))
        assertEquals(AppFailure.Storage(StorageKind.UNKNOWN), classify(RefreshFailureReason.Storage))
    }

    @Test
    fun `refresh unauthorized is service rejection rather than required or revoked authentication`() {
        listOf(null, BackendErrorDetails(9001, "private refresh detail")).forEach { backend ->
            assertEquals(
                AppFailure.Service(ServiceKind.CLIENT_REJECTED, 401, backend?.reasonCode),
                classify(RefreshFailureReason.Http(401, backend)),
            )
        }
    }

    @Test
    fun `refresh HTTP preserves actual status reason and service category`() {
        val cases = mapOf(
            399 to ServiceKind.OTHER_HTTP,
            400 to ServiceKind.CLIENT_REJECTED,
            403 to ServiceKind.ACCESS_DENIED,
            418 to ServiceKind.CLIENT_REJECTED,
            429 to ServiceKind.RATE_LIMITED,
            499 to ServiceKind.CLIENT_REJECTED,
            500 to ServiceKind.UNAVAILABLE,
            503 to ServiceKind.UNAVAILABLE,
            599 to ServiceKind.UNAVAILABLE,
            600 to ServiceKind.OTHER_HTTP,
        )
        cases.forEach { (status, kind) ->
            listOf(null, BackendErrorDetails(401, "private refresh detail")).forEach { backend ->
                assertEquals(
                    "Refresh HTTP $status with backend ${backend != null}",
                    AppFailure.Service(kind, status, backend?.reasonCode),
                    classify(RefreshFailureReason.Http(status, backend)),
                )
            }
        }
    }

    private fun classify(reason: RefreshFailureReason): AppFailure =
        classifier.fromNetwork(NetworkFailure.RefreshFailed(reason))
}
