package com.quizzgame.core.error.ui.common

import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.ui.status.QuizzStatusIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ErrorUiMapperTest {
    private val mapper = ErrorUiMapper { "local:$it" }

    @Test
    fun everyProductionCopyKeyMapsToItsLocalResourceAndSemanticIcon() {
        val cases = mapOf(
            ErrorCopyKey.CONNECTION_PROBLEM to (R.string.quizz_error_connection_problem to QuizzStatusIcon.NETWORK),
            ErrorCopyKey.REQUEST_TIMED_OUT to (R.string.quizz_error_request_timed_out to QuizzStatusIcon.NETWORK),
            ErrorCopyKey.SERVER_UNAVAILABLE to (R.string.quizz_error_server_unavailable to QuizzStatusIcon.SERVER),
            ErrorCopyKey.TOO_MANY_REQUESTS to (R.string.quizz_error_too_many_requests to QuizzStatusIcon.SERVER),
            ErrorCopyKey.ACCESS_DENIED to (R.string.quizz_error_access_denied to QuizzStatusIcon.AUTH),
            ErrorCopyKey.SIGN_IN_REQUIRED to (R.string.quizz_error_sign_in_required to QuizzStatusIcon.AUTH),
            ErrorCopyKey.SESSION_EXPIRED to (R.string.quizz_error_session_expired to QuizzStatusIcon.AUTH),
            ErrorCopyKey.UNEXPECTED_RESPONSE to (R.string.quizz_error_unexpected_response to QuizzStatusIcon.GENERIC),
            ErrorCopyKey.STORAGE_PROBLEM to (R.string.quizz_error_storage_problem to QuizzStatusIcon.GENERIC),
            ErrorCopyKey.GENERIC_FAILURE to (R.string.quizz_error_generic_failure to QuizzStatusIcon.GENERIC),
        )
        assertEquals(ErrorCopyKey.entries.filterNot { it.name == "MOCK_CONFIGURATION" }.toSet(), cases.keys)
        cases.forEach { (key, expected) ->
            val model = mapper.map(spec().copy(title = key))
            assertEquals(key.name, "local:${expected.first}", model.title)
            assertEquals(key.name, expected.second, model.icon)
            assertNull(model.message)
            assertEquals(emptyList<ErrorUiAction>(), model.actions)
        }
    }

    @Test
    fun everyActionUsesItsLocalLabelWithoutAddingActions() {
        val resources = mapOf(
            ErrorActionKind.RETRY to R.string.quizz_error_action_retry,
            ErrorActionKind.CONFIRM_RETRY to R.string.quizz_error_action_confirm_retry,
            ErrorActionKind.GO_BACK to R.string.quizz_error_action_go_back,
            ErrorActionKind.SIGN_IN to R.string.quizz_error_action_sign_in,
            ErrorActionKind.OPEN_SETTINGS to R.string.quizz_error_action_open_settings,
            ErrorActionKind.DISMISS to R.string.quizz_error_action_dismiss,
            ErrorActionKind.EXIT to R.string.quizz_error_action_exit,
        )
        assertEquals(ErrorActionKind.entries.toSet(), resources.keys)
        resources.forEach { (kind, resource) ->
            val result = mapper.map(spec().copy(actions = listOf(kind)))
            assertEquals(listOf(ErrorUiAction(kind, "local:$resource")), result.actions)
        }
    }

    @Test
    fun optionalMessageAndOwnerActionOrderArePreserved() {
        val order = listOf(ErrorActionKind.OPEN_SETTINGS, ErrorActionKind.DISMISS, ErrorActionKind.GO_BACK)
        val result = mapper.map(spec().copy(message = ErrorCopyKey.CONNECTION_PROBLEM, actions = order))
        assertEquals("local:${R.string.quizz_error_connection_problem}", result.message)
        assertEquals(order, result.actions.map { it.kind })
    }

    @Test
    fun specialisedAdaptersRejectEveryMismatchedSurface() {
        listOf(ErrorSurface.BLOCKING, ErrorSurface.BANNER, ErrorSurface.INLINE, ErrorSurface.TRANSIENT)
            .forEach { expected ->
                ErrorSurface.entries.forEach { actual ->
                    val spec = spec().copy(surface = actual)
                    if (expected == actual) {
                        requireErrorSurface(spec, expected)
                    } else {
                        assertThrows(IllegalArgumentException::class.java) { requireErrorSurface(spec, expected) }
                    }
                }
            }
    }

    private fun spec() = ErrorPresentationSpec(ErrorSurface.BLOCKING, ErrorCopyKey.GENERIC_FAILURE)
}
