plugins {
    alias(libs.plugins.quizz.android.infrastructure.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quizzgame.core.session"
}

dependencies {
    api(projects.core.network)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.tink)
    implementation(libs.tink.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
