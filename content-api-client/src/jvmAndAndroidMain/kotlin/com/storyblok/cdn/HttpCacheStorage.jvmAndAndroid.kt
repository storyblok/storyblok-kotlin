package com.storyblok.cdn

import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.FileStorage
import kotlinx.coroutines.Dispatchers
import java.io.File

internal actual fun fileCacheStorage(name: String): CacheStorage? {
    val directory = httpCacheDirectory(name) ?: return null
    return FileStorage(directory, Dispatchers.IO)
}

/**
 * Where the [name] cache lives on disk, or `null` if no directory could be resolved.
 *
 * Android points `java.io.tmpdir` at the app's cache directory
 * (`ActivityThread.handleBindApplication` does
 * `System.setProperty("java.io.tmpdir", context.getCacheDir().getAbsolutePath())`), so this resolves
 * to `context.cacheDir` without the library needing a `Context`. On desktop JVM it is the machine's
 * temp directory; apps that want the cache somewhere durable can point the property at their own
 * cache directory on startup, see the caching section of the README.
 */
internal fun httpCacheDirectory(name: String): File? {
    val cacheDir = System.getProperty("java.io.tmpdir")?.takeIf { it.isNotBlank() } ?: return null
    return File(cacheDir, "$HTTP_CACHE_DIRECTORY_NAME/$name")
}
