package com.example.jetnews

import androidx.compose.ui.window.ComposeViewport

/**
 * The web entry point. `ComposeViewport` renders the shared UI into a canvas that fills the
 * document body and follows its size.
 */
fun main() {
    ComposeViewport {
        JetNewsApp(draft = true)
    }
}
