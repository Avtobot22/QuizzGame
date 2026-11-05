# QuizzGame

Квиз для компании друзей с приложениями для Android TV и Android-телефонов. Ведущий запускает игру на телевизоре, участники подключаются с телефонов по QR-коду комнаты.

Проект в разработке: сейчас готовится основа приложений, игровые сценарии ещё не реализованы.

## Запуск

Нужны Android Studio, JDK 21 для приложения, JDK 17 для mock SDK, Android SDK Platform 37.0 и Build Tools 36.0.0. Для запуска подойдут телефон или Android TV с Android 8.0 (API 26) и выше, либо соответствующий эмулятор.

1. После клонирования загрузи mock SDK:

   ```sh
   git submodule update --init --recursive
   ```

2. Открой корень проекта в Android Studio, выбери JDK 21 для Gradle и выполни Sync.
3. Выбери `app/mobile` для телефона или `app/tv` для телевизора, укажи устройство и нажми Run.

Собрать оба debug APK из терминала:

```sh
./gradlew :app:mobile:assembleDebug :app:tv:assembleDebug
```

В Windows используй `gradlew.bat`.
