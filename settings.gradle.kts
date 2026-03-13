pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "storyblok-kotlin"
include(
    ":ktor-client-plugin",
    ":content-api-client",
    ":compose-runtime",
    ":compose-material3",
    ":examples"
)
