plugins {
    alias(libs.plugins.quizz.android.mobile.library)
}

android {
    namespace = "com.quizzgame.core.error.ui.mobile"
}

dependencies {
    api(projects.core.error.uiCommon)
    implementation(projects.core.designsystem.mobile)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.kotlinx.coroutines.test)
}
