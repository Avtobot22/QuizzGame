plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
}

android {
    namespace = "com.quizzgame.core.error"
}

dependencies {
    api(projects.core.network)
    api(projects.core.session)
}
