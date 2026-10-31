plugins {
    alias(libs.plugins.quizz.android.mobile.library)
}

android {
    namespace = "com.quizzgame.core.designsystem.mobile"
}

dependencies {
    api(projects.core.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
