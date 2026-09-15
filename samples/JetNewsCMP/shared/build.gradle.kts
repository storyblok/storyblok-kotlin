@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.example.jetnews.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        // Compose Multiplatform resources are packaged as Android assets.
        androidResources { enable = true }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    // Only the targets the Storyblok Compose SDK publishes for Apple: an Intel simulator
    // slice would not resolve.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    wasmJs {
        browser()
    }

    // One source set for every target that is an app rather than the Visual Editor preview, so
    // they can share a single `contentVersion` actual.
    applyDefaultHierarchyTemplate {
        common {
            group("app") {
                withIos()
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.material3)
            api(libs.navigation3.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.runtime.compose)

            api(libs.storyblok.compose)
            api(libs.storyblok.material3)

            // The BOM covers the Coil modules storyblok-material3 pulls in transitively too:
            // every one has to agree with this Compose Multiplatform release's Skiko.
            implementation(project.dependencies.platform(libs.coil.bom))
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.kotlinx.datetime)
        }
    }

    compilerOptions {
        optIn.addAll(
            "kotlin.uuid.ExperimentalUuidApi",
            "kotlinx.coroutines.ExperimentalCoroutinesApi",
            "kotlinx.coroutines.FlowPreview",
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "androidx.compose.ui.ExperimentalComposeUiApi",
            "org.jetbrains.compose.resources.ExperimentalResourceApi",
        )
    }
}

compose.resources {
    packageOfResClass = "com.example.jetnews.resources"
}
