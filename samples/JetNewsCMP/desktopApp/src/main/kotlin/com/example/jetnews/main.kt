package com.example.jetnews

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/**
 * The desktop entry point. Like the other three, it does nothing but show [JetNewsApp].
 *
 * The window opens at a phone-ish aspect ratio because the JetNews layouts are the Android
 * sample's, and none of them adapt to a wide window yet.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "JetNews",
        state = rememberWindowState(size = DpSize(480.dp, 900.dp)),
    ) {
        JetNewsApp(draft = true)
    }
}
