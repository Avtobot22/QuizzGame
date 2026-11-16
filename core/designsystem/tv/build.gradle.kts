plugins {
    alias(libs.plugins.quizz.android.tv.library)
}

android {
    namespace = "com.quizzgame.core.designsystem.tv"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(projects.core.coroutines)
    api(projects.core.media)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
    api(projects.core.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
