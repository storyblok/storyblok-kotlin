import org.gradle.kotlin.dsl.register

plugins {
//    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
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
        includes.from("README.md")
        suppressInheritedMembers.set(true)
        failOnWarning.set(true)
    }
    pluginsConfiguration.html {
        customAssets.from("images/logo-icon.svg")
    }
    dokkaSourceSets.configureEach {
        externalDocumentationLinks.register("ktor") {
            url("https://api.ktor.io/")
        }
    }
}