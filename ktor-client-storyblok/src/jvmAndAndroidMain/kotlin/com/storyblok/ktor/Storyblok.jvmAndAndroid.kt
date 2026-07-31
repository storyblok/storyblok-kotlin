package com.storyblok.ktor

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIOEngineConfig

internal actual fun HttpClientConfig<*>.configureEngine() {
    engine {
        if (this !is CIOEngineConfig) return@engine
        maxConnectionsCount = 30
    }
}