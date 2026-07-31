package com.storyblok.cdn

import io.ktor.client.plugins.cache.storage.CacheStorage

internal const val HTTP_CACHE_DIRECTORY_NAME: String = "storyblok-http-cache"

/**
 * Names of the two caches [HttpCache][io.ktor.client.plugins.cache.HttpCache] keeps. They get
 * separate directories: both key entries by the hash of the request URL, so sharing a directory
 * would let a public and a private response for the same URL overwrite one another.
 */
internal const val PUBLIC_CACHE_NAME: String = "public"
internal const val PRIVATE_CACHE_NAME: String = "private"

/**
 * A persistent, file-backed [CacheStorage] for the platform, held in a [name] subdirectory of the
 * cache directory. `null` when the platform has no file cache implementation or no cache directory
 * could be resolved.
 *
 * Ktor only ships `FileStorage` for the JVM (and therefore Android) in the version this module
 * builds against; the migration to a common, `kotlinx.io`-based implementation
 * ([ktor#4940](https://github.com/ktorio/ktor/pull/4940)) is merged but unreleased. Returning
 * `null` leaves [HttpCache][io.ktor.client.plugins.cache.HttpCache] on its default in-memory
 * storage.
 */
internal expect fun fileCacheStorage(name: String): CacheStorage?

/**
 * Resolves the storages backing [HttpCache][io.ktor.client.plugins.cache.HttpCache].
 *
 * The file cache is process-wide and outlives the [HttpClient][io.ktor.client.HttpClient] that
 * created it, so tests replace this to keep each client isolated.
 */
internal var httpCacheStorage: (name: String) -> CacheStorage? = ::fileCacheStorage
