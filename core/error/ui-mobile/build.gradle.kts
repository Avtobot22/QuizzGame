plugins {
    alias(libs.plugins.quizz.android.mobile.library)
}

android {
    namespace = "com.quizzgame.core.error.ui.mobile"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(projects.core.error.uiCommon)
    implementation(projects.core.designsystem.mobile)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.kotlinx.coroutines.test)
}
