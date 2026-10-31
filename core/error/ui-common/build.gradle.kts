plugins {
    alias(libs.plugins.quizz.android.shared.library)
}

android {
    namespace = "com.quizzgame.core.error.ui.common"
}

dependencies {
    api(projects.core.error.foundation)
    api(projects.core.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
