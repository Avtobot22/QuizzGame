package com.quizzgame.core.error

import com.quizzgame.core.network.BackendErrorDetails
import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.network.RefreshFailureReason
import com.quizzgame.core.session.SessionState
import com.quizzgame.core.session.SessionStorageFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.lang.reflect.Modifier

class ErrorDiagnosticPrivacyTest {

    private val classifier: ErrorClassifier = DefaultErrorClassifier()
    private val operation = ErrorOperationKey.from(Operations.FEATURE_LOAD)
    private val secrets = listOf(
        "Bearer SECRET_ACCESS_TOKEN",
        "SECRET_REFRESH_TOKEN",
        "SECRET_PENDING_TOKEN",
        "user@example.test",
        "otp=123456",
        "https://private.example.test/path?key=SECRET_QUERY",
        "{\"password\":\"SECRET_CREDENTIAL\"}",
    )

    @Test
    fun `direct and nested refresh errors keep only category status and numeric reason`() {
        val backend = BackendErrorDetails(9001, secrets.joinToString(" "), secrets.reversed().joinToString(" "))
        val cases = mapOf(
            NetworkFailure.Http(503, backend) to ErrorDiagnosticCategory.SERVICE_UNAVAILABLE,
            NetworkFailure.Http(401, backend) to ErrorDiagnosticCategory.AUTHENTICATION_REQUIRED,
            NetworkFailure.RefreshFailed(RefreshFailureReason.Http(503, backend)) to
                ErrorDiagnosticCategory.SERVICE_UNAVAILABLE,
            NetworkFailure.RefreshFailed(RefreshFailureReason.Http(401, backend)) to
                ErrorDiagnosticCategory.CLIENT_REJECTED,
        )
        val captured = mutableListOf<ErrorDiagnostic>()
        val sink = ErrorDiagnostics { captured += it }
        cases.forEach { (technical, category) ->
            val semantic = classifier.fromNetwork(technical)
            val diagnostic = ErrorDiagnostic.from(semantic, operation)
            sink.record(diagnostic)
            assertEquals(category, diagnostic.category)
            assertEquals(9001, diagnostic.reasonCode)
            secrets.forEach { secret ->
                assertFalse(diagnostic.toString().contains(secret))
                val context = ErrorOperationContext(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT)
                assertFalse(DefaultErrorDecisionPolicy().decide(semantic, context).toString().contains(secret))
            }
        }
        assertEquals(cases.size, captured.size)
        assertFalse(captured.toString().contains("SECRET"))
    }

    @Test
    fun `session storage and refresh storage do not export session state or invent HTTP metadata`() {
        SessionStorageFailure.entries.forEach { reason ->
            val state = SessionState.Unavailable(reason, SessionState.Authenticated(123456789))
            val diagnostic = ErrorDiagnostic.from(classifier.fromSession(state), operation)
            assertEquals(null, diagnostic.status)
            assertEquals(null, diagnostic.reasonCode)
            assertFalse(diagnostic.toString().contains("123456789"))
            assertFalse(diagnostic.toString().contains("Authenticated"))
        }
        val refresh = classifier.fromNetwork(NetworkFailure.RefreshFailed(RefreshFailureReason.Storage))
        assertEquals(
            ErrorDiagnostic(operation, ErrorDiagnosticCategory.STORAGE_UNKNOWN),
            ErrorDiagnostic.from(refresh, operation),
        )
    }

    @Test
    fun `diagnostic schema contains exactly the approved fields`() {
        val fields = ErrorDiagnostic::class.java.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }
        assertEquals(
            setOf("operationKey", "category", "status", "reasonCode", "diagnosticId"),
            fields.map { it.name }.toSet(),
        )
        assertFalse(fields.any { Throwable::class.java.isAssignableFrom(it.type) })
    }

    private enum class Operations {
        FEATURE_LOAD,
    }
}
