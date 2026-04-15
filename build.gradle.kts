plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.dokka)
}

dependencies {
    dokka(project(":ktor-client-storyblok"))
    dokka(project(":content-api-client"))
    dokka(project(":storyblok-compose"))
    dokka(project(":storyblok-material3"))
}

dokka {
    moduleName.set("Storyblok Kotlin")
    dokkaPublications.html {
        includes.from("Module.md")
        includes.from("CHANGELOG.md")
        failOnWarning.set(true)

    }
    pluginsConfiguration {
        html {
            customAssets.from("images/logo-icon.svg")
        }
        versioning {
            version.set(libs.versions.android.compileSdk.get())
//            olderVersionsDir.set(projectDir.resolve("dokka-docs"))
        }
    }
    dokkaSourceSets.configureEach {
        externalDocumentationLinks.register("ktor") {
            url("https://api.ktor.io/")
        }
    }
}
