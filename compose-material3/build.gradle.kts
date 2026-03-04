@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

dokka {
    moduleName.set("Storyblok Compose Material 3")
    dokkaSourceSets.configureEach {
//        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/storyblok/storyblok-kotlin/compose-material3/")
            remoteLineSuffix.set("#L")
        }
    }
}

kotlin {
    explicitApi()
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }
    jvm()
    js {
        browser()
        nodejs()
    }
    androidLibrary {
        namespace = "com.storyblok.composeMaterial3"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    macosArm64()
    macosX64()
    wasmJs {
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(project(":compose-runtime"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmTest.dependencies {
            implementation(libs.logback.classic)
        }

        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("com.storyblok", "storyblok-compose-material3", libs.versions.storyblok.kotlin.get())

    pom {
        name = "storyblok-compose-material3"
        description = "An SDK to integrate Storyblok with Compose Material 3"
        inceptionYear = "2026"
        url = "https://github.com/storyblok/storyblok-kotlin"
        licenses {
            license {
                name = "The MIT License (MIT)"
                url = "https://github.com/storyblok/storyblok-kotlin?tab=MIT-1-ov-file"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "storyblok"
                name = "Storyblok"
                url = "https://www.storyblok.com/"
            }
        }
        scm {
            url = "https://github.com/storyblok/storyblok-kotlin"
            connection = "scm:git:https://github.com/storyblok/storyblok-kotlin"
            developerConnection = "scm:git:ssh://git@github.com/storyblok/storyblok-kotlin"
        }
    }
}
