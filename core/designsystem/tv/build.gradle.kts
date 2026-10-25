plugins {
    alias(libs.plugins.quizz.android.tv.library)
}

android {
    namespace = "com.quizzgame.core.designsystem.tv"
}

dependencies {
    api(projects.core.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
