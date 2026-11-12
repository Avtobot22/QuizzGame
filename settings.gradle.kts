pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx\\..*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx\\..*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "QuizzGame"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app:mobile")
include(":app:tv")
include(":app:foundation")
include(":app:debug-support")
include(":core:ui")
include(":core:designsystem:mobile")
include(":core:designsystem:tv")
include(":core:network")
include(":core:presentation")
include(":core:feature-flags")
include(":feature:splash:tv")
include(":feature:splash:mobile")
include(":shared:splash")
include(":core:session")
include(":core:navigation")
include(":core:error:foundation")
include(":core:error:ui-common")
include(":core:error:ui-mobile")
include(":core:error:ui-tv")

val mockSdkDirectory = file("external/android-mock-server")
check(mockSdkDirectory.resolve("settings.gradle.kts").isFile) {
    "Initialize the pinned mock SDK: git submodule update --init --recursive"
}
includeBuild(mockSdkDirectory) {
    dependencySubstitution {
        substitute(module("dev.androidmock:mock-core")).using(project(":mock-core"))
        substitute(module("dev.androidmock:mock-okhttp-server")).using(project(":mock-okhttp-server"))
    }
}

val flagsSdkDirectory = file("external/feature-flags")
check(flagsSdkDirectory.resolve("settings.gradle.kts").isFile) {
    "Initialize the pinned feature flags SDK: git submodule update --init --recursive"
}
includeBuild(flagsSdkDirectory) {
    dependencySubstitution {
        substitute(module("local.featureflags:core")).using(project(":feature-flags-core"))
        substitute(module("local.featureflags:json")).using(project(":feature-flags-json"))
        substitute(module("local.featureflags:android")).using(project(":feature-flags-android"))
        substitute(module("local.featureflags:retrofit")).using(project(":feature-flags-retrofit"))
    }
}
