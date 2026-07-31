package com.storyblok.cdn

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileCacheStorageTest {

    private val root = createTempDirectory("storyblok-cache-test").toFile()

    @AfterTest
    fun cleanUp() {
        httpCacheStorage = ::fileCacheStorage
        root.deleteRecursively()
    }

    /**
     * Storyblok marks draft responses `private` and published responses `public`, so both paths
     * need a storage or draft responses would silently go uncached.
     */
    @Test
    fun `public and private responses are cached in their own directories`() = runBlocking {
        httpCacheStorage = { name -> FileStorage(File(root, name), Dispatchers.IO) }

        val client = HttpClient(MockEngine { request ->
                val cacheControl = when (request.url.parameters["version"]) {
                    "draft" -> "max-age=0, private, must-revalidate"
                    else -> "max-age=0, public, s-maxage=604800"
                }
                respond(
                    """{"story": { "content": {}}}""",
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("${ContentType.Application.Json}"),
                        HttpHeaders.CacheControl to listOf(cacheControl)
                    )
                )
        }) { configureStoryblok(Json) { accessToken = "mock-api-key" } }

        client.get("stories/published-slug")
        client.get("stories/draft-slug") { url.parameters["version"] = "draft" }

        assertTrue(File(root, PUBLIC_CACHE_NAME).isDirectory, "public cache directory not created")
        assertTrue(File(root, PRIVATE_CACHE_NAME).isDirectory, "private cache directory not created")
        assertEquals(1, File(root, PUBLIC_CACHE_NAME).listFiles()?.size, "public response not cached")
        assertEquals(1, File(root, PRIVATE_CACHE_NAME).listFiles()?.size, "private response not cached")
    }

    /**
     * The cache follows `java.io.tmpdir` so that apps can relocate it — and Coil's image cache,
     * which reads the same property through okio — by setting the property on startup.
     */
    @Test
    fun `cache directory follows the java io tmpdir property`() {
        val directory = assertNotNull(httpCacheDirectory(PUBLIC_CACHE_NAME))

        assertEquals(PUBLIC_CACHE_NAME, directory.name)
        assertEquals(HTTP_CACHE_DIRECTORY_NAME, directory.parentFile.name)

        val tmp = File(System.getProperty("java.io.tmpdir")).absolutePath
        assertTrue(
            directory.absolutePath.startsWith(tmp),
            "expected a directory under $tmp, was ${directory.absolutePath}"
        )
    }

    @Test
    fun `cache directory moves with the java io tmpdir property`() {
        val original = System.getProperty("java.io.tmpdir")
        try {
            System.setProperty("java.io.tmpdir", root.absolutePath)
            val directory = assertNotNull(httpCacheDirectory(PUBLIC_CACHE_NAME))
            assertEquals(File(root, "$HTTP_CACHE_DIRECTORY_NAME/$PUBLIC_CACHE_NAME"), directory)
        } finally {
            System.setProperty("java.io.tmpdir", original)
        }
    }
}
