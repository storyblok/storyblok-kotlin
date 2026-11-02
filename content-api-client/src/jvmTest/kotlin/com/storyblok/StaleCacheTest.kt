package com.storyblok

import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.paging.asItemSnapshotListFlow
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.configureStoryblok
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.stories
import com.storyblok.cdn.story
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * A copy cached before the content changed shape, here before the page component gained its required `related` field,
 * gives way to the fresh copy instead of failing before that copy arrives.
 */
class StaleCacheTest {

    @Serializable @SerialName("page")
    class Pg(val title: String, val related: List<String>) : Component()

    @AfterTest
    fun restoreCacheStorage() {
        httpCacheStorage = ::fileCacheStorage
    }

    /** The content of the response the endpoint answers the next request with. */
    private var content = ""

    /**
     * The `Cache-Control` the endpoint answers with. By default as it answers a draft request: stale a second later,
     * when a cached-only read is still answered from the cache and any other goes to the network.
     */
    private var cacheControl = "max-age=0, private, must-revalidate"

    /** As the endpoint answers a published request, which the client keeps fresh for the `s-maxage` of a week. */
    private val published = "max-age=0, public, s-maxage=604800"

    /** The status the endpoint answers with, as an unreachable or failing server would answer instead of the content. */
    private var status = HttpStatusCode.OK

    /** How many requests reached the endpoint, rather than being answered from the cache. */
    private var requests = 0

    private val client: StoryblokClientImpl by lazy {
        // An in-memory cache per client: the JVM's is file-backed and outlives the process.
        httpCacheStorage = { CacheStorage.Unlimited() }
        val json = Json {
            classDiscriminator = "component"
            ignoreUnknownKeys = true
            explicitNulls = false
            serializersModule = SerializersModule {
                polymorphic(Component::class) { defaultDeserializer { serializer<Component.Unknown>() } }
                polymorphic(Component::class, Pg::class, Pg.serializer())
            }
        }
        val engine = MockEngine { request ->
            requests++
            val story = storyJson(content)
            respond(
                if (request.url.encodedPath.endsWith("/stories")) """{"stories":[$story]}""" else """{"story":$story}""",
                status,
                headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    HttpHeaders.CacheControl to listOf(cacheControl),
                    "Total" to listOf("1"),
                    "Per-Page" to listOf("25"),
                ),
            )
        }
        StoryblokClientImpl({}, {}, {}, json, HttpClient(engine) { configureStoryblok(json) { accessToken = "tok" } })
    }

    private fun storyJson(content: String) = """
        {"id":1,"uuid":"00000000-0000-0000-0000-000000000001","name":"Home","content":$content,
         "slug":"home","full_slug":"home","created_at":"2025-07-09T14:35:26.851Z","position":0,"tag_list":[],
         "is_startpage":false,"group_id":"57350688-5a28-49d1-b5a9-086ae0d4c0d2","lang":"default","alternates":[]}
    """.trimIndent()

    private val before = """{"_uid":"c","component":"page","title":"before"}"""
    private val after = """{"_uid":"c","component":"page","title":"after","related":[]}"""

    /** The titles of the stories `story("home")` emits when the cache holds [cached] and the network [loaded]. */
    private fun titles(cached: String, loaded: String): List<String> = runBlocking {
        content = cached
        client.http.get("stories/home")
        delay(1.seconds)
        content = loaded
        client.story<Pg>("home").map { it.content.title }.toList()
    }

    @Test
    fun `a cached story that no longer decodes gives way to the fresh one`() {
        assertEquals(listOf("after"), titles(cached = before, loaded = after))
    }

    @Test
    fun `a cached story that decodes is still emitted before the fresh one`() {
        val cached = """{"_uid":"c","component":"page","title":"cached","related":[]}"""
        assertEquals(listOf("cached", "after"), titles(cached = cached, loaded = after))
    }

    @Test
    fun `a fresh story that does not decode still fails`() {
        assertFailsWith<SerializationException> { titles(cached = before, loaded = before) }
    }

    @Test
    fun `a cached story that no longer decodes is revalidated rather than read again while it is fresh`() {
        cacheControl = published
        assertEquals(listOf("after"), titles(cached = before, loaded = after))
    }

    @Test
    fun `a cached page that no longer decodes gives way to the fresh one`() {
        cache(before)
        val (errors, titles) = firstPage()
        assertTrue(errors.isEmpty(), "a load error was reported: $errors")
        assertEquals(listOf("after"), titles)
    }

    @Test
    fun `a cached page that no longer decodes is revalidated rather than read again while it is fresh`() {
        cacheControl = published
        cache(before)
        val (errors, titles) = firstPage()
        assertTrue(errors.isEmpty(), "a load error was reported: $errors")
        assertEquals(listOf("after"), titles)
    }

    @Test
    fun `a page that does not decode from the network is not requested again`() {
        content = before
        val (errors) = firstPage(loaded = before)
        assertIs<SerializationException>(errors.firstOrNull(), "errors: $errors")
        assertEquals(1, requests)
    }

    @Test
    fun `a cached page that no longer decodes still reports its own failure when revalidating it fails`() {
        cacheControl = published
        cache(before)
        status = HttpStatusCode.NotFound
        val (errors) = firstPage(loaded = before)
        assertIs<SerializationException>(errors.firstOrNull(), "errors: $errors")
    }

    /** Caches the first page of stories as the endpoint answers it with [cached] as each story's content. */
    private fun cache(cached: String) = runBlocking {
        content = cached
        client.http.get("stories") {
            // As the pager asks for the first page of stories of the page component.
            parameter("page", "1")
            parameter("per_page", "25")
            parameter("content_type", "page")
        }
        requests = 0
    }

    /**
     * The load errors reported, and the titles of the first page a collector sees, once the network has the page with
     * [loaded] as each story's content. Stops at the first error, as the page will not arrive after one.
     */
    private fun firstPage(loaded: String = after): Pair<List<Throwable>, List<String>?> = runBlocking {
        content = loaded
        val errors = mutableListOf<Throwable>()
        val titles = withTimeoutOrNull(5_000) {
            client.stories<Pg>(PagingConfig(pageSize = 25)).flow
                .asItemSnapshotListFlow { states ->
                    // A source's own failed load shows under `source`, as the mediator's state stands for the whole.
                    listOfNotNull(states.source, states.mediator)
                        .flatMap { listOf(it.refresh, it.append, it.prepend) }
                        .filterIsInstance<LoadState.Error>()
                        .forEach { errors += it.error }
                }
                .first { it.items.isNotEmpty() || errors.isNotEmpty() }
                .items.map { it.content.title }
        }
        errors to titles
    }
}
