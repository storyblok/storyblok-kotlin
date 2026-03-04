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
}

dokka {
    moduleName.set("Storylok Content Delivery API Client")
    dokkaSourceSets.configureEach {
//        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/storyblok/storyblok-kotlin/content-api-client/")
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
        namespace = "com.storyblok.contentApiClient"
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
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    wasmJs {
        nodejs()
    }
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    sourceSets {
        commonMain.dependencies {
            api(project(":ktor-client-plugin"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.mock)
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
    coordinates("com.storyblok", "storyblok-content-api", libs.versions.storyblok.kotlin.get())

    pom {
        name = "storyblok-content-api"
        description = "A client for Storyblok's Content Delivery API"
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
