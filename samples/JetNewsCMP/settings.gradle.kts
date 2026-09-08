pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // Left at the default PREFER_PROJECT: the Kotlin/Wasm plugin registers the Node.js
    // distribution repository on the project, and FAIL_ON_PROJECT_REPOS rejects it.
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "JetNewsCMP"
include(":shared")
include(":androidApp")
include(":desktopApp")
include(":webApp")
