plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
}

android {
    namespace = "com.quizzgame.core.presentation"
}

dependencies {
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.bundles.unit.test)
}
