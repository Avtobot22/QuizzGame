package com.quizzgame.core.error

/**
 * Ограниченный ключ операции из enum вызывающей feature, без адресов, ввода и идентификаторов пользователя.
 *
 * Имена операций задаются в исходниках, например `enum class Operations { FEED_LOAD, FEED_REFRESH }`.
 * [from] принимает только имя enum в формате UPPER_SNAKE_CASE длиной до 64 символов.
 * Состав enum и смысл операции принадлежат feature; core не содержит имён предметных сценариев.
 * Для различения feature используй префикс в самом имени константы: имя класса enum в ключ не входит.
 * Имя является стабильным диагностическим контрактом; его переименование меняет ключ для получателя.
 */
@JvmInline
value class ErrorOperationKey private constructor(val value: String) {

    companion object {
        /** Создаёт ключ из статического имени enum; не использует переопределённый `toString()`. */
        fun from(operation: Enum<*>): ErrorOperationKey {
            require(KEY_PATTERN.matches(operation.name)) { "Operation key must be a bounded UPPER_SNAKE_CASE name" }
            return ErrorOperationKey(operation.name)
        }

        private val KEY_PATTERN = Regex("[A-Z][A-Z0-9_]{0,63}")
    }
}
