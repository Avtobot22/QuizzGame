package com.quizzgame.core.error.operation

/**
 * Ключ диагностики берётся из статического имени enum, без пользовательских данных.
 * Для различения фич нужен префикс в имени константы: имя класса enum в ключ не входит.
 * Переименование константы меняет диагностический ключ.
 */
@JvmInline
value class ErrorOperationKey private constructor(val value: String) {

    companion object {

        fun from(operation: Enum<*>): ErrorOperationKey {
            require(KEY_PATTERN.matches(operation.name)) { "Operation key must be a bounded UPPER_SNAKE_CASE name" }
            return ErrorOperationKey(operation.name)
        }

        private val KEY_PATTERN = Regex("[A-Z][A-Z0-9_]{0,63}")
    }
}
