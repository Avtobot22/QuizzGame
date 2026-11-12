plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
}

android {
    namespace = "com.quizzgame.core.flags"
}

dependencies {
    api(libs.featureflags.core)
    implementation(libs.featureflags.json)
    implementation(libs.featureflags.android)
    api(projects.core.network)
    testImplementation(libs.bundles.unit.test)
}
