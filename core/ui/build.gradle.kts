plugins {
    alias(libs.plugins.quizz.android.shared.library)
}

android {
    namespace = "com.quizzgame.core.ui"
}

dependencies {
    api(projects.core.media.android)
    implementation(libs.coil.compose)
}
