# QuizzGame

Квиз для компании друзей: ведущий запускает игру на Android TV, участники подключаются с телефонов по QR комнаты.

Проект в разработке. Готовы отдельные приложения для телефона и ТВ, темы и компоненты дизайн-системы. Игровые экраны, комнаты и авторизация пока не реализованы; при запуске приложения показывают пустой экран.

## Сборка и запуск

Нужны JDK 21, Android SDK Platform 37.0, Build Tools 36.0.0 и Android Studio с поддержкой версии AGP из [version catalog](gradle/libs.versions.toml). Приложения поддерживают Android 8.0 (API 26) и выше.

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

Изменения проходят через PR в `main`. Перед отправкой запусти `qualityCheck`; при изменениях TV-интерфейса проверь управление пультом, фокус и Back.
