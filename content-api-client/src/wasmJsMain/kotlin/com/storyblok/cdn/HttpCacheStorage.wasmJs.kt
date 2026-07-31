package com.storyblok.cdn

import io.ktor.client.plugins.cache.storage.CacheStorage

// Ktor has no FileStorage outside the JVM in the version this module builds against.
internal actual fun fileCacheStorage(name: String): CacheStorage? = null
