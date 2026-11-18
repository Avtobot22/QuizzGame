package com.quizzgame.core.media.android

import com.quizzgame.core.media.MediaSpec
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EmbeddedMediaTest {
    @Test
    fun tinyPngIsAcceptedAndWrongMimeOrInvalidBase64IsRejected() {
        val png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aKp8AAAAASUVORK5CYII="
        assertNotNull(MediaSpec.Embedded("image/png", png).validatedImageBytes())
        assertNull(MediaSpec.Embedded("image/jpeg", png).validatedImageBytes())
        assertNull(MediaSpec.Embedded("image/png", "not base64!").validatedImageBytes())
        assertNull(MediaSpec.Embedded("image/png", "A".repeat(174768)).validatedImageBytes())
    }
}
