plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.dokka)
}

group = "com.storyblok"
version = "0.1.0"

dependencies {
    dokka(project(":ktor-client-plugin"))
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
            version.set("0.1.0")
//            olderVersionsDir.set(projectDir.resolve("dokka-docs"))
        }
    }
    dokkaSourceSets.configureEach {
        externalDocumentationLinks.register("ktor") {
            url("https://api.ktor.io/")
        }
    }
}