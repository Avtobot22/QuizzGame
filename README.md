# QuizzGame

Квиз для компании друзей: ведущий запускает игру на Android TV, участники подключаются с телефонов по QR комнаты.

Проект в разработке. Готовы отдельные приложения для телефона и ТВ, темы и компоненты дизайн-системы. Игровые экраны, комнаты и авторизация пока не реализованы; при запуске приложения показывают пустой экран.

Сетевой транспорт OkHttp/Retrofit выделен в [core/network](core/network). `NetworkClient.results` возвращает `NetworkResult<T>` с технической причиной ошибки, включая отдельный сбой refresh.

`createNetworkJson()` задаёт общие настройки JSON. Один экземпляр передаётся в `NetworkClientFactory` для конвертеров API/refresh и адаптеров результатов.

Отладочные ошибки mock-транспорта, refresh и классификатора компилируются только в debug-вариантах `core/network`, `core/session` и [core/error](core/error). В release их типов нет.

Для Retrofit suspend-методов используй `results.call { ... }`, для `Response<T>` - `results.response { ... }`, для блокирующего `Call<T>` - `results.execute(call)`.

Если endpoint допускает пустой ответ `204`, объяви suspend-метод с типом `Unit`; обязательный DTO без тела считается `InvalidResponse`. Отмена корутины распространяется как отмена.

## Сборка и запуск

Нужны JDK 21 для приложения, JDK 17 для mock SDK, Android SDK Platform 37.0, Build Tools 36.0.0 и Android Studio с поддержкой версии AGP из [version catalog](gradle/libs.versions.toml). Приложения поддерживают Android 8.0 (API 26) и выше.

После клонирования загрузи закреплённые исходники mock SDK:

```sh
git submodule update --init --recursive
```

1. Открой корень проекта в Android Studio и выбери JDK 21 для Gradle.
2. Укажи путь к SDK в `local.properties` (`sdk.dir=...`) или через `ANDROID_HOME`.
3. Выполни Gradle Sync и выбери модуль `:app:mobile` для телефона или `:app:tv` для телевизора.

Собрать оба debug APK из терминала:

```sh
./gradlew :app:mobile:assembleDebug :app:tv:assembleDebug
```

APK появятся в `app/mobile/build/outputs/apk/debug/` и `app/tv/build/outputs/apk/debug/`. В Windows используй `gradlew.bat`.

## Модули

| Модуль | Содержимое |
|---|---|
| `app/mobile` | Приложение участника на телефоне |
| `app/tv` | Приложение ведущего на телевизоре |
| `core/ui` | Общая палитра, шрифты и UI-ресурсы |
| `core/network` | HTTP-клиенты, JSON, политики запросов и типизированный результат сетевого вызова |
| `core/session` | Защищённое хранение токенов, состояние сессии и ID установки |
| [core/error](core/error) | Семантическая классификация сетевых ошибок и ошибок хранилища сессии |
| `core/designsystem/mobile` | Темы и компоненты Material 3 |
| `core/designsystem/tv` | Темы и компоненты TV Material |

Код написан на Kotlin и Jetpack Compose. Общие настройки сборки находятся в `buildSrc`, версии зависимостей - в `gradle/libs.versions.toml`.

## Проверки

```sh
./gradlew qualityCheck
```

Команда запускает Detekt, Android lint и unit-test tasks, собирает debug/release и APK инструментальных тестов обоих приложений. Отчёты сохраняются в `build/reports/` соответствующих модулей.

Правила Kotlin-анализа заданы в [конфиге Detekt](config/detekt/detekt.yml), настройки редактора - в [.editorconfig](.editorconfig). Проверка не меняет исходники; для исправления форматирования запусти `./gradlew detekt --auto-correct`, затем проверь diff и повтори `./gradlew detekt`.

Для UI-тестов подключи телефон или TV-устройство и запусти нужную команду отдельно:

```sh
./gradlew :app:mobile:connectedDebugAndroidTest
./gradlew :app:tv:connectedDebugAndroidTest
```

UI-тесты используют Kakao Compose со screen objects и Espresso для аппаратных клавиш и Android View. Общие тестовые зависимости подключаются через convention plugins, версии закреплены в version catalog.

Изменения проходят через PR в `main`. Перед отправкой запусти `qualityCheck`; при изменениях TV-интерфейса проверь управление пультом, фокус и Back.
