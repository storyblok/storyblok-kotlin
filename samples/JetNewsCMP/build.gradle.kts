// Top-level build file. Every plugin the modules use is declared here so that the
// version is resolved once and each module can apply it without repeating it.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
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
