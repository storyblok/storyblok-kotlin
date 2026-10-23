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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // First, so a publishToMavenLocal build of the SDK wins over the same version on Central.
        mavenLocal {
            content {
                includeGroup("com.storyblok")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "JetNews"
include(":app")
 