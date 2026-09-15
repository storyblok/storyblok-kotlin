package com.example.jetnews

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * The iOS entry point, called from `iosApp/iosApp/ContentView.swift`.
 */
@Suppress("unused", "FunctionName")
fun MainViewController(): UIViewController = ComposeUIViewController {
    JetNewsApp()
}
