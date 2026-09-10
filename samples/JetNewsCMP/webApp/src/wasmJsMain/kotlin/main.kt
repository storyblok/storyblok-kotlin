package com.example.jetnews

import androidx.compose.ui.window.ComposeViewport

/**
 * The web entry point. `ComposeViewport` renders the shared UI into a canvas that fills the
 * document body and follows its size.
 */
fun main() {
    ComposeViewport {
        // Always draft: there is no build type here to key off, the way Android uses
        // `BuildConfig.DEBUG`. A bundle from `wasmJsBrowserDistribution` that is actually
        // deployed should pass `false`, or read a Gradle property set at build time.
        JetNewsApp(draft = true)
    }
}
