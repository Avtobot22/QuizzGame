import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

private object AndroidConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 26
    const val JDK = 21
}

private fun CommonExtension.configureAndroid() {
    compileSdk = AndroidConfig.COMPILE_SDK
    defaultConfig.minSdk = AndroidConfig.MIN_SDK
    buildFeatures.compose = true
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint.apply {
        warningsAsErrors = true
        informational += "AndroidGradlePluginVersion"
    }
}

private fun Project.configureKotlin() {
    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(AndroidConfig.JDK)
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

fun Project.addCatalogDependency(configuration: String, alias: String) {
    val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies.add(configuration, catalog.findLibrary(alias).get())
}

private fun Project.configureCompose(configuration: String = "implementation") {
    val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies.add(configuration, dependencies.platform(catalog.findLibrary("androidx-compose-bom").get()))
    addCatalogDependency(configuration, "androidx-compose-foundation")
    addCatalogDependency(configuration, "androidx-compose-ui")
}

fun Project.configureAndroidApplication() {
    extensions.configure<ApplicationExtension> {
        configureAndroid()
        defaultConfig {
            targetSdk = AndroidConfig.COMPILE_SDK
            versionCode = 1
            versionName = "0.1.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        buildTypes {
            getByName("debug") {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
            }
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            }
        }
    }
    configureKotlin()
    configureCompose()
    addCatalogDependency("implementation", "androidx-activity-compose")
    val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies.add(
        "androidTestImplementation",
        dependencies.platform(catalog.findLibrary("androidx-compose-bom").get())
    )
    addCatalogDependency("androidTestImplementation", "androidx-compose-ui-test-junit4")
    addCatalogDependency("androidTestImplementation", "androidx-test-junit")
    addCatalogDependency("androidTestImplementation", "androidx-test-runner")
    addCatalogDependency("debugImplementation", "androidx-compose-ui-test-manifest")
}

fun Project.configureAndroidLibrary() {
    extensions.configure<LibraryExtension> { configureAndroid() }
    configureKotlin()
    configureCompose("api")
}
