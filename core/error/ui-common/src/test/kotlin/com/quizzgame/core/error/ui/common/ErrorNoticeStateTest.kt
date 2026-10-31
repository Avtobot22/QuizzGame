package com.quizzgame.core.error.ui.common

import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorNoticeStateTest {
    @Test
    fun staleActionCannotDismissANewerDeliveryWithTheSameSpecification() {
        val state = ErrorNoticeState()
        val spec = ErrorPresentationSpec(ErrorSurface.TRANSIENT, ErrorCopyKey.CONNECTION_PROBLEM)
        val previous = state.replace(spec)
        val current = state.replace(spec)

        assertNotSame(previous, current)
        assertFalse(state.dismiss(previous))
        assertSame(current, state.current)
        assertTrue(state.dismiss(current))
        assertNull(state.current)
    }

    @Test
    fun onlyFirstActionCanConsumeTheCurrentDelivery() {
        val state = ErrorNoticeState()
        val notice = state.replace(ErrorPresentationSpec(ErrorSurface.TRANSIENT, ErrorCopyKey.GENERIC_FAILURE))

        assertTrue(state.dismiss(notice))
        assertFalse(state.dismiss(notice))
        assertNull(state.current)
    }
}
