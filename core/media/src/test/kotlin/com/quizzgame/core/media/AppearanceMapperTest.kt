package com.quizzgame.core.media

import com.quizzgame.core.media.data.dto.ColorDto
import com.quizzgame.core.media.data.dto.MediaDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceMapperTest {
    @Test
    fun hexUsesTrailingAlphaAndRetainsBothThemeValues() {
        assertEquals(ColorSpec.Hex(0x80112233, 0x80112233), color("""{"kind":"hex","value":"#11223380"}"""))
        assertEquals(
            ColorSpec.Hex(0xFF000000, 0xFFFFFFFF),
            color("""{"kind":"hex","light":"#000000","dark":"#FFFFFF"}"""),
        )
        assertEquals(ColorSpec.Hex(0x00112233, 0x00112233), color("""{"kind":"hex","value":"#11223300"}"""))
    }

    @Test
    fun mixedPartialAndMalformedOptionalColorsAreIgnored() {
        listOf(
            """{"kind":"hex","value":"#112233","dark":"#FFFFFF"}""",
            """{"kind":"hex","light":"#000000"}""",
            """{"kind":"hex","value":"#XYZXYZ"}""",
        ).forEach { assertNull(color(it)) }
    }

    @Test
    fun mediaAcceptsOnlyRegisteredLocalKeysAndNewKinds() {
        assertTrue(media("""{"kind":"icon","name":"home"}""") is MediaSpec.Icon)
        assertTrue(media("""{"kind":"asset","name":"cover"}""") is MediaSpec.Asset)
        assertTrue(media("""{"kind":"remote","url":"https://cdn.test/a?signature=secret"}""") is MediaSpec.Remote)
        assertTrue(media("""{"kind":"embedded","mimeType":"image/png","base64":"AA=="}""") is MediaSpec.Embedded)
        listOf(
            """{"kind":"remote","url":"file:///etc/passwd"}""",
            """{"kind":"remote","url":"http://cdn.test/a"}""",
            """{"kind":"icon","name":"../../secret"}""",
            """{"kind":"asset","name":"unknown"}""",
            """{"kind":"embedded","mimeType":"image/svg+xml","base64":"AA=="}""",
        ).forEach { assertEquals(MediaSpec.Placeholder, media(it)) }
    }

    @Test
    fun optionalPresentationIsBoundedAndSecretsAreRedacted() {
        val media = media(
            """{"kind":"remote","url":"https://cdn.test/a?secret=token",
          "presentation":{"aspectRatio":1000,"blurRadius":100,"transition":{"kind":"fade","durationMs":9000}}}""",
        )
        assertEquals(16f / 9f, media.presentation.aspectRatio)
        assertEquals(24f, media.presentation.blurRadius)
        assertEquals(600, media.presentation.durationMs)
        assertEquals("Remote(<redacted>)", media.toString())
    }

    @Test
    fun excessiveEmbeddedPayloadIsRejectedBeforeBinaryDecoding() {
        val input = """{"kind":"embedded","mimeType":"image/png","base64":"${"A".repeat(174768)}"}"""
        assertEquals(MediaSpec.Placeholder, media(input))
    }

    @Test
    fun unknownKindsAndMalformedRequiredValuesFailInTheSerializer() {
        assertThrows(SerializationException::class.java) { color("""{"kind":"future"}""") }
        assertThrows(SerializationException::class.java) { color("""{"kind":"token","name":23}""") }
        assertThrows(SerializationException::class.java) { media("""{"kind":"appleRemote"}""") }
        assertThrows(SerializationException::class.java) { media("""{"kind":"remote"}""") }
        assertThrows(SerializationException::class.java) {
            media(
                """{"kind":"icon","name":"home",
            "presentation":{"transition":{"kind":"future"}}}""",
            )
        }
    }

    private fun color(value: String) = AppearanceMapper.color(Json.decodeFromString<ColorDto>(value))
    private fun media(
        value: String,
    ) = AppearanceMapper.media(Json.decodeFromString<MediaDto>(value), setOf("home"), setOf("cover"))
}
