package com.quizzgame.core.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ErrorOperationKeyTest {

    @Test
    fun `key uses caller enum name instead of arbitrary toString text`() {
        assertEquals("FEATURE_LOAD", ErrorOperationKey.from(Operations.FEATURE_LOAD).value)
        assertEquals(ErrorOperationKey.from(Operations.FEATURE_LOAD), ErrorOperationKey.from(Other.FEATURE_LOAD))
    }

    @Test
    fun `names outside bounded static key format are rejected`() {
        Unsafe.entries.forEach { operation ->
            assertThrows(IllegalArgumentException::class.java) { ErrorOperationKey.from(operation) }
        }
    }

    @Test
    fun `namespaced operations keep distinct keys`() {
        assertEquals(
            setOf("FEATURE_LOAD", "FEATURE_REFRESH"),
            Operations.entries.map { ErrorOperationKey.from(it).value }.toSet(),
        )
    }

    private enum class Operations {
        FEATURE_LOAD,
        FEATURE_REFRESH,
        ;

        override fun toString(): String = "Bearer SECRET_TOKEN otp=123456 https://private.test/?key=SECRET"
    }

    private enum class Other {
        FEATURE_LOAD,
    }

    // Некорректные имена проверяют отказ валидатора ключей; исключение ограничено этим enum.
    @Suppress("EnumNaming", "EnumEntryNameCase")
    private enum class Unsafe {
        lowercase,
        `otp=123456`,
        THIS_OPERATION_KEY_IS_LONGER_THAN_SIXTY_FOUR_CHARACTERS_AND_IS_REJECTED,
    }
}
