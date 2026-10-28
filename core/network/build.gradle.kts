plugins {
    alias(libs.plugins.quizz.kotlin.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.okhttp)
    api(libs.retrofit)
    api(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
