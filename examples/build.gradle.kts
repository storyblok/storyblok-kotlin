@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
    jvm()
    js {
        browser {
            testTask {
                useMocha { timeout = "5s" }
            }
        }
        nodejs {
            testTask {
                useMocha { timeout = "5s" }
            }
        }
    }
    android {
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
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    wasmJs {
        nodejs()
    }
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()

    sourceSets {

        commonTest.dependencies {
            implementation(project(":ktor-client-storyblok"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.logback.classic)
        }
    }

    // Generated content-api-client tests live in src/contentClientTest (not commonTest), because content-api-client
    // (via androidx.paging) supports fewer targets than this module.
    val contentClientTest = sourceSets.create("contentClientTest") {
        dependsOn(sourceSets.getByName("commonTest"))
        dependencies {
            implementation(project(":content-api-client"))
            implementation(libs.androidx.paging.common)
        }
    }

    // Targets that can't host the content-client tests: content-api-client omits androidNative*/iosX64, and the
    // asItemSnapshotListFlow tests use runBlocking (real time, since runTest's virtual clock doesn't drive Paging),
    // which js/wasmJs don't have. That leaves JVM + Native. commonTest stays buildable on all of them.
    val contentClientExcludedTargets =
        setOf("androidNativeArm32", "androidNativeArm64", "androidNativeX64", "androidNativeX86", "iosX64", "js", "wasmJs")
    targets.configureEach {
        if (name !in contentClientExcludedTargets) {
            compilations.configureEach {
                if (name == "test") {
                    defaultSourceSet.dependsOn(contentClientTest)
                }
            }
        }
    }
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    standalone.set(false)
    device.set("booted")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
