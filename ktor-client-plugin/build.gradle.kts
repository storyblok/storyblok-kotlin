@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
}

dokka {
    moduleName.set("Storyblok Ktor Client Plugin")
    dokkaSourceSets.configureEach {
        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/storyblok/storyblok-kotlin/ktor-client-plugin/")
            remoteLineSuffix.set("#L")
        }
        externalDocumentationLinks.register("ktor") {
            url("https://api.ktor.io/")
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
        namespace = "com.storyblok"
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
            api(libs.ktor.client)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        jvmMain.dependencies {
            api(libs.ktor.client.cio)
        }

        androidMain.dependencies {
            api(libs.ktor.client.cio)
        }

        linuxMain.dependencies {
            api(libs.ktor.client.curl)
        }

        appleMain.dependencies {
            api(libs.ktor.client.darwin)
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
        }

        jsMain.dependencies {
            api(libs.ktor.client.js)
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

tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("com.storyblok", "ktor-client-storyblok", libs.versions.storyblok.kotlin.get())

    pom {
        name = "ktor-client-storyblok"
        description = "A plugin to simplify calling Storyblok's APIs with the Ktor Client."
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
            developerConnection = "scm:git:https://github.com/storyblok/storyblok-kotlin"
        }
    }
}
