package com.quizzgame.core.error.ui.mobile.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorSnackbarTest {
    @Test
    fun primaryActionUsesNativeSnackbarAndReturnsOnlyTheSelectedIntent() = runTest {
        val host = SnackbarHostState()
        val result = async { host.showErrorSnackbar(spec(), ::text) }
        runCurrent()
        val data = checkNotNull(host.currentSnackbarData)
        assertEquals(text(R.string.quizz_error_connection_problem), data.visuals.message)
        assertEquals(text(R.string.quizz_error_action_retry), data.visuals.actionLabel)
        assertEquals(SnackbarDuration.Indefinite, data.visuals.duration)
        assertTrue(data.visuals.withDismissAction)
        data.performAction()
        assertEquals(ErrorActionKind.RETRY, result.await())
        assertNull(host.currentSnackbarData)
    }

    @Test
    fun dismissalDoesNotTriggerABusinessAction() = runTest {
        val host = SnackbarHostState()
        val result = async { host.showErrorSnackbar(spec(), ::text) }
        runCurrent()
        checkNotNull(host.currentSnackbarData).dismiss()
        assertNull(result.await())
    }

    @Test
    fun informationalErrorUsesShortDurationAndCombinesLocalTitleAndMessage() = runTest {
        val host = SnackbarHostState()
        val result = async {
            host.showErrorSnackbar(
                spec().copy(message = ErrorCopyKey.GENERIC_FAILURE, actions = emptyList()),
                ::text,
            )
        }
        runCurrent()
        val data = checkNotNull(host.currentSnackbarData)
        assertEquals(
            "${text(R.string.quizz_error_connection_problem)}\n${text(R.string.quizz_error_generic_failure)}",
            data.visuals.message,
        )
        assertEquals(SnackbarDuration.Short, data.visuals.duration)
        assertNull(data.visuals.actionLabel)
        assertFalse(data.visuals.withDismissAction)
        data.dismiss()
        assertNull(result.await())
    }

    @Test
    fun persistentSurfacesAndMultiplePrimaryActionsAreRejected() = runTest {
        listOf(
            ErrorSurface.BANNER,
            ErrorSurface.INLINE,
            ErrorSurface.BLOCKING,
            ErrorSurface.SILENT,
        ).forEach { surface ->
            val error = runCatching {
                SnackbarHostState().showErrorSnackbar(spec().copy(surface = surface, actions = emptyList()), ::text)
            }.exceptionOrNull()
            assertTrue(error is IllegalArgumentException)
        }
        val error = runCatching {
            SnackbarHostState().showErrorSnackbar(
                spec().copy(actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.GO_BACK)),
                ::text,
            )
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    private fun spec() = ErrorPresentationSpec(
        ErrorSurface.TRANSIENT,
        ErrorCopyKey.CONNECTION_PROBLEM,
        actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS),
    )

    private fun text(resource: Int) = "local:$resource"
}
