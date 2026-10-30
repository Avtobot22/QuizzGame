package com.quizzgame.core.error

import com.quizzgame.core.network.BackendErrorDetails
import com.quizzgame.core.network.NetworkFailure
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorClassifierTest {

    private val classifier: ErrorClassifier = DefaultErrorClassifier()

    @Test
    fun `connection timeout and invalid response have distinct semantics`() {
        assertEquals(
            AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE),
            classifier.fromNetwork(NetworkFailure.Connection),
        )
        assertEquals(
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
            classifier.fromNetwork(NetworkFailure.Timeout),
        )
        assertEquals(AppFailure.InvalidServerData, classifier.fromNetwork(NetworkFailure.InvalidResponse))
    }

    @Test
    fun `HTTP status selects semantic kind and preserves unknown backend reason`() {
        val cases = mapOf(
            399 to ServiceKind.OTHER_HTTP,
            400 to ServiceKind.CLIENT_REJECTED,
            403 to ServiceKind.ACCESS_DENIED,
            408 to ServiceKind.CLIENT_REJECTED,
            418 to ServiceKind.CLIENT_REJECTED,
            422 to ServiceKind.CLIENT_REJECTED,
            429 to ServiceKind.RATE_LIMITED,
            499 to ServiceKind.CLIENT_REJECTED,
            500 to ServiceKind.UNAVAILABLE,
            503 to ServiceKind.UNAVAILABLE,
            599 to ServiceKind.UNAVAILABLE,
            600 to ServiceKind.OTHER_HTTP,
        )
        cases.forEach { (status, kind) ->
            listOf(null, BackendErrorDetails(9001, "server detail", "server copy")).forEach { backend ->
                assertEquals(
                    "HTTP $status with backend ${backend != null}",
                    AppFailure.Service(kind, status, backend?.reasonCode),
                    classifier.fromNetwork(NetworkFailure.Http(status, backend)),
                )
            }
        }
    }

    @Test
    fun `all server errors preserve status rather than collapsing to 503`() {
        (500..599).forEach { status ->
            assertEquals(
                AppFailure.Service(ServiceKind.UNAVAILABLE, status),
                classifier.fromNetwork(NetworkFailure.Http(status)),
            )
        }
    }

    @Test
    fun `HTTP unauthorized keeps metadata and does not prove revocation`() {
        listOf(null, BackendErrorDetails(-17, "private detail")).forEach { backend ->
            assertEquals(
                AppFailure.Authentication(AuthenticationKind.REQUIRED, 401, backend?.reasonCode),
                classifier.fromNetwork(NetworkFailure.Http(401, backend)),
            )
        }
    }

    @Test
    fun `missing credentials and proven revocation are distinct without invented HTTP metadata`() {
        assertEquals(
            AppFailure.Authentication(AuthenticationKind.REQUIRED),
            classifier.fromNetwork(NetworkFailure.AuthenticationRequired),
        )
        assertEquals(
            AppFailure.Authentication(AuthenticationKind.REVOKED),
            classifier.fromNetwork(NetworkFailure.SessionRevoked),
        )
    }

    @Test
    fun `backend reason cannot override actual HTTP status`() {
        assertEquals(
            AppFailure.Service(ServiceKind.UNAVAILABLE, 503, 401),
            classifier.fromNetwork(NetworkFailure.Http(503, BackendErrorDetails(401, "unauthorized"))),
        )
    }
}
