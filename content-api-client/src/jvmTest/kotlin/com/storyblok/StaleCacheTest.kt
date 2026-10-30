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
            val story = storyJson(content)
            respond(
                if (request.url.encodedPath.endsWith("/stories")) """{"stories":[$story]}""" else """{"story":$story}""",
                HttpStatusCode.OK,
                headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    // As the API answers a draft request: stale a second later, when a cached-only read is still
                    // answered from the cache and any other goes to the network.
                    HttpHeaders.CacheControl to listOf("max-age=0, private, must-revalidate"),
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
    fun `a cached page that no longer decodes gives way to the fresh one`(): Unit = runBlocking {
        // A first collection caches the page as it was before the field was added, failing to read it.
        content = before
        withTimeoutOrNull(5_000) {
            client.stories<Pg>(PagingConfig(pageSize = 25)).flow
                .asItemSnapshotListFlow { }
                .first { it.items.isNotEmpty() }
        }

        content = after
        val errors = mutableListOf<Throwable>()
        val titles = withTimeoutOrNull(5_000) {
            client.stories<Pg>(PagingConfig(pageSize = 25)).flow
                .asItemSnapshotListFlow { states ->
                    // A source's own failed load shows under `source`, as the mediator's state stands for the whole.
                    listOf(states.source.refresh, states.source.append, states.source.prepend)
                        .filterIsInstance<LoadState.Error>()
                        .forEach { errors += it.error }
                }
                .first { it.items.isNotEmpty() }
                .items.map { it.content.title }
        }

        assertTrue(errors.isEmpty(), "a load error was reported: $errors")
        assertEquals(listOf("after"), titles)
    }
}
