package com.quizzgame.core.error.ui.common

import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.ui.status.QuizzStatusIcon
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugErrorUiMapperTest {
    @Test
    fun mockConfigurationUsesDebugLocalCopyAndGenericIcon() {
        val result = ErrorUiMapper { "local:$it" }.map(
            ErrorPresentationSpec(ErrorSurface.BLOCKING, ErrorCopyKey.MOCK_CONFIGURATION),
        )
        assertEquals("local:${R.string.quizz_error_mock_configuration}", result.title)
        assertEquals(QuizzStatusIcon.GENERIC, result.icon)
    }
}
