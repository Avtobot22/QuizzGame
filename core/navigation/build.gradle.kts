plugins {
    alias(libs.plugins.quizz.android.shared.library)
}

android { namespace = "com.quizzgame.core.navigation" }

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
