package com.quizzgame.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class ErrorDiagnosticTest {

    private val operation = ErrorOperationKey.from(Operations.FEATURE_LOAD)

    @Test
    fun `connection invalid data and storage preserve distinct semantic categories`() {
        val cases = mapOf(
            AppFailure.Connectivity(ConnectivityKind.OFFLINE_OR_UNREACHABLE) to
                ErrorDiagnosticCategory.OFFLINE_OR_UNREACHABLE,
            AppFailure.Connectivity(ConnectivityKind.TIMEOUT) to ErrorDiagnosticCategory.TIMEOUT,
            AppFailure.InvalidServerData to ErrorDiagnosticCategory.INVALID_SERVER_DATA,
            AppFailure.Storage(StorageKind.READ) to ErrorDiagnosticCategory.STORAGE_READ,
            AppFailure.Storage(StorageKind.WRITE) to ErrorDiagnosticCategory.STORAGE_WRITE,
            AppFailure.Storage(StorageKind.CRYPTO) to ErrorDiagnosticCategory.STORAGE_CRYPTO,
            AppFailure.Storage(StorageKind.CORRUPT) to ErrorDiagnosticCategory.STORAGE_CORRUPT,
            AppFailure.Storage(StorageKind.UNKNOWN) to ErrorDiagnosticCategory.STORAGE_UNKNOWN,
        )
        cases.forEach { (failure, category) ->
            assertEquals(ErrorDiagnostic(operation, category), ErrorDiagnostic.from(failure, operation))
        }
    }

    @Test
    fun `service category follows classification while preserving actual status and numeric reason`() {
        val cases = mapOf(
            ServiceKind.UNAVAILABLE to ErrorDiagnosticCategory.SERVICE_UNAVAILABLE,
            ServiceKind.RATE_LIMITED to ErrorDiagnosticCategory.RATE_LIMITED,
            ServiceKind.ACCESS_DENIED to ErrorDiagnosticCategory.ACCESS_DENIED,
            ServiceKind.CLIENT_REJECTED to ErrorDiagnosticCategory.CLIENT_REJECTED,
            ServiceKind.OTHER_HTTP to ErrorDiagnosticCategory.OTHER_HTTP,
        )
        assertEquals(ServiceKind.entries.toSet(), cases.keys)
        cases.forEach { (kind, category) ->
            listOf(401, 418, 429, 503, 599, 600).forEach { status ->
                listOf(null, 9001).forEach { reason ->
                    assertEquals(
                        ErrorDiagnostic(operation, category, status, reason),
                        ErrorDiagnostic.from(AppFailure.Service(kind, status, reason), operation),
                    )
                }
            }
        }
    }

    @Test
    fun `authentication requirement and proven revocation remain distinct`() {
        val cases = mapOf(
            AuthenticationKind.REQUIRED to ErrorDiagnosticCategory.AUTHENTICATION_REQUIRED,
            AuthenticationKind.REVOKED to ErrorDiagnosticCategory.SESSION_REVOKED,
        )
        assertEquals(AuthenticationKind.entries.toSet(), cases.keys)
        cases.forEach { (kind, category) ->
            assertEquals(
                ErrorDiagnostic(operation, category, 401, 71),
                ErrorDiagnostic.from(AppFailure.Authentication(kind, 401, 71), operation),
            )
            assertEquals(
                ErrorDiagnostic(operation, category),
                ErrorDiagnostic.from(AppFailure.Authentication(kind), operation),
            )
        }
    }

    @Test
    fun `caller supplied UUID is preserved and absent ID is never generated`() {
        val failure = AppFailure.Connectivity(ConnectivityKind.TIMEOUT)
        val id = UUID.fromString("9d07927a-a4ea-4ca2-884d-321c1eec2360")
        val first = ErrorDiagnostic.from(failure, operation, id)
        assertEquals(id, first.diagnosticId)
        assertEquals(first, ErrorDiagnostic.from(failure, operation, id))
        assertNull(ErrorDiagnostic.from(failure, operation).diagnosticId)
    }

    private enum class Operations {
        FEATURE_LOAD,
    }
}
