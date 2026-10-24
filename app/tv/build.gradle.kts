plugins {
    alias(libs.plugins.quizz.android.tv.application)
}

android {
    namespace = "com.quizzgame.tv"
    defaultConfig { applicationId = "com.quizzgame.tv" }
}

dependencies {
    implementation(projects.core.designsystem.tv)
}
