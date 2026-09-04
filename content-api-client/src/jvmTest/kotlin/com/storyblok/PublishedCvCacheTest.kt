package com.storyblok

import androidx.paging.PagingConfig
import androidx.paging.asItemSnapshotListFlow
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.configureStoryblok
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.stories
import com.storyblok.ktor.Api
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * A passive collector — [asItemSnapshotListFlow], as opposed to Compose's `collectAsLazyPagingItems` — only ever
 * sees what the [androidx.paging.PagingSource] reads out of Ktor's HTTP cache. Nothing re-drives a load for it, so
 * a page the cache does not answer for never arrives at all rather than arriving late.
 */
class PublishedCvCacheTest {

    @Serializable @SerialName("page")
    class Pg(val title: String) : Component()

    @AfterTest
    fun restoreCacheStorage() {
        httpCacheStorage = ::fileCacheStorage
    }

    @Test
    fun `draft content reaches a passive collector`() {
        val (items, _) = collectFirstPage(Api.Config.Version.Draft, cvRedirect = false)
        assertEquals(listOf("t1", "t2"), items)
    }

    @Test
    fun `published content reaches a passive collector when no cv redirect intervenes`() {
        val (items, _) = collectFirstPage(Api.Config.Version.Published, cvRedirect = false)
        assertEquals(listOf("t1", "t2"), items)
    }

    @Test
    fun `a cv redirect whose parameter order matches the next request is cached and read back`() {
        // The control for the test below: the same redirect, differing only in where `cv` sits in the query.
        val (items, _) = collectFirstPage(Api.Config.Version.Published, cvRedirect = true, cvLast = false)
        assertEquals(listOf("t1", "t2"), items)
    }

    @Test
    fun `published content behind a cv redirect reaches a passive collector`() {
        val (items, requested) = collectFirstPage(Api.Config.Version.Published, cvRedirect = true, cvLast = true)
        assertEquals(listOf("t1", "t2"), items, "requested: $requested")
    }

    @Test
    fun `a cold cache publishes no empty page even while the network is slow`() {
        // Delaying the engine holds the network back and gives an empty page every chance to appear first.
        val (sizes, requests) = collectEmissions(networkDelay = 300)

        assertTrue(sizes.none { it == 0 }, "an empty page reached the collector: $sizes")
        assertEquals(2, sizes.last(), "the loaded page never arrived, got $sizes")
        // And the waiting costs nothing: the mediator's refresh is the only fetch, as it was before.
        assertEquals(1, requests, "a cold read should not add a request of its own")
    }

    @Test
    fun `a cold cache publishes no empty page when the network is fast`() {
        val (sizes, requests) = collectEmissions(networkDelay = 0)

        assertTrue(sizes.none { it == 0 }, "an empty page reached the collector: $sizes")
        assertEquals(2, sizes.last(), "the loaded page never arrived, got $sizes")
        assertEquals(1, requests, "a cold read should not add a request of its own")
    }

    /** The item count of every snapshot a passive collector sees, and how many requests reached the network. */
    private fun collectEmissions(networkDelay: Long): Pair<List<Int>, Int> {
        val requested = mutableListOf<String>()
        val client = client(
            Api.Config.Version.Draft,
            cvRedirect = false,
            cvLast = true,
            requested = requested,
            networkDelay = networkDelay,
        )
        val sizes = mutableListOf<Int>()
        runBlocking {
            withTimeoutOrNull(4_000) {
                client.stories<Pg>(PagingConfig(pageSize = 2)).flow
                    .asItemSnapshotListFlow()
                    .collect { sizes += it.items.size }
            }
        }
        return sizes to requested.size
    }

    /** The titles of the first non-empty page a passive collector sees, and the URLs that reached the network. */
    private fun collectFirstPage(
        version: Api.Config.Version,
        cvRedirect: Boolean,
        cvLast: Boolean = true,
    ): Pair<List<String>?, List<String>> {
        val requested = mutableListOf<String>()
        val client = client(version, cvRedirect, cvLast, requested)
        val items = runBlocking {
            withTimeoutOrNull(8_000) {
                client.stories<Pg>(PagingConfig(pageSize = 2)).flow
                    .asItemSnapshotListFlow()
                    .first { it.items.isNotEmpty() }
                    .items.map { it.content.title }
            }
        }
        return items to requested
    }

    /**
     * A client talking to a mock of the endpoint, with its own in-memory HTTP cache — the JVM's is file-backed and
     * outlives the process, so sharing it would let one case answer another's requests.
     */
    private fun client(
        version: Api.Config.Version,
        cvRedirect: Boolean,
        cvLast: Boolean,
        requested: MutableList<String>,
        networkDelay: Long = 0,
    ): StoryblokClientImpl {
        httpCacheStorage = { CacheStorage.Unlimited() }

        val json = Json {
            isLenient = true
            decodeEnumsCaseInsensitive = true
            classDiscriminator = "component"
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            serializersModule = SerializersModule {
                polymorphic(Component::class) { defaultDeserializer { serializer<Component.Unknown>() } }
                polymorphic(Component::class, Pg::class, Pg.serializer())
            }
            useAlternativeNames = true
        }

        val engine = MockEngine { request ->
            requested += request.url.toString().substringAfter("?")
            if (networkDelay > 0) delay(networkDelay)
            if (cvRedirect && request.url.parameters["cv"] == null) {
                respond("", HttpStatusCode.MovedPermanently, headersOf(HttpHeaders.Location to listOf(
                    if (cvLast) {
                        // As the API does: cv appended to the end of the URL that was asked for.
                        URLBuilder(request.url).apply { parameters.append("cv", "123") }.buildString()
                    } else {
                        // As DefaultRequest would have built it: cv ahead of token and version.
                        val query = request.url.parameters.entries()
                            .joinToString("&") { (name, values) -> "$name=${values.first()}" }
                            .replace("&token=", "&cv=123&token=")
                        "${request.url.protocol.name}://${request.url.host}${request.url.encodedPath}?$query"
                    }
                )))
            } else {
                respond(
                    """{"stories":[${storyJson(1)},${storyJson(2)}],"rels":[],"cv":123}""",
                    HttpStatusCode.OK,
                    headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        HttpHeaders.CacheControl to listOf("public, s-maxage=60"),
                        HttpHeaders.Date to listOf("Wed, 02 Sep 2026 12:00:00 GMT"),
                        "Total" to listOf("2"),
                        "Per-Page" to listOf("2"),
                    ),
                )
            }
        }

        val http = HttpClient(engine) { configureStoryblok(json) { accessToken = "tok"; this.version = version } }
        return StoryblokClientImpl({}, {}, {}, json, http)
    }

    private fun storyJson(id: Int) = """
        {"id":$id,"uuid":"00000000-0000-0000-0000-00000000000$id","name":"Story $id",
         "content":{"_uid":"c$id","component":"page","title":"t$id"},
         "slug":"s-$id","full_slug":"a/s-$id","created_at":"2025-07-09T14:35:26.851Z",
         "position":0,"tag_list":[],"is_startpage":false,
         "group_id":"57350688-5a28-49d1-b5a9-086ae0d4c0d2","lang":"default","alternates":[]}
    """.trimIndent()
}
