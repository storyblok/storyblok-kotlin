@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "jetnews.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
        }
    }

    compilerOptions {
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
    }
}
