plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    // Supplies Dispatchers.Main on the AWT event thread, which `collectAsStateWithLifecycle`
    // needs; nothing else on the desktop classpath provides it.
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.example.jetnews.MainKt"

        nativeDistributions {
            packageName = "JetNews"
            packageVersion = "1.0.0"
        }
    }
}
