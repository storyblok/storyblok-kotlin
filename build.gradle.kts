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

// ws below 8.21.0 is affected by CVE-2026-45736 and CVE-2026-48779. Nothing here depends on it
// directly: it arrives transitively through the Kotlin/JS and Kotlin/Wasm test tooling, pinned to
// exact versions, so regenerating the lockfiles alone cannot move it. Force it for both yarn roots
// instead, then refresh the locks with kotlinUpgradeYarnLock and kotlinWasmUpgradeYarnLock.
plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.extensions
        .getByType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>()
        .resolution("ws", "8.21.3")
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin> {
    rootProject.extensions
        .getByType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootExtension>()
        .resolution("ws", "8.21.3")
}
