plugins {
    alias(libs.plugins.quizz.kotlin.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.okhttp)
    testImplementation(libs.junit)
}
