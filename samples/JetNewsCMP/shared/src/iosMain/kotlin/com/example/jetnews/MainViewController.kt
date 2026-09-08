package com.example.jetnews

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * The iOS entry point, called from `iosApp/iosApp/ContentView.swift`.
 *
 * Draft content is always requested here: the sample has no release iOS build, and
 * `BuildConfig.DEBUG` — what the Android entry point keys off — has no Kotlin/Native equivalent.
 */
@Suppress("unused", "FunctionName")
fun MainViewController(): UIViewController = ComposeUIViewController {
    JetNewsApp(draft = true)
}
