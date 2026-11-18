plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.quizz.android.hilt)
}
android { namespace = "com.quizzgame.shared.quizentry" }
dependencies {
    api(projects.shared.catalog)
    api(projects.core.error.foundation)
    api(projects.core.network)
    implementation(projects.core.session)
    implementation(projects.core.coroutines)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.bundles.unit.test)
}
