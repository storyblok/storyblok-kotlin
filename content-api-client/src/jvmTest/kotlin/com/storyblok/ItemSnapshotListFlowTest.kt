package com.storyblok

import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.paging.asItemSnapshotListFlow
import com.storyblok.cdn.StoryblokClientException
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.stories
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class ItemSnapshotListFlowTest {

    @Serializable @SerialName("page")
    class Pg(val title: String) : Component()

    private fun storyJson(id: Int) = """
        {"id":$id,"uuid":"00000000-0000-0000-0000-00000000000$id","name":"Story $id",
         "content":{"_uid":"c$id","component":"page","title":"t$id"},
         "slug":"s-$id","full_slug":"a/s-$id","created_at":"2025-07-09T14:35:26.851Z",
         "position":0,"tag_list":[],"is_startpage":false,
         "group_id":"57350688-5a28-49d1-b5a9-086ae0d4c0d2","lang":"default","alternates":[]}
    """.trimIndent()

    private fun client(status: HttpStatusCode = HttpStatusCode.OK, total: String? = "4") =
        StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Pg::class, Pg.serializer()) },
            jsonBuilder = { ignoreUnknownKeys = true; explicitNulls = false; coerceInputValues = true },
            http = HttpClient(MockEngine { req ->
                if (status != HttpStatusCode.OK) {
                    respond("""{"error":"nope"}""", status,
                        headersOf(HttpHeaders.ContentType to listOf("application/json")))
                } else {
                    val page = req.url.parameters["page"]!!.toInt()
                    val ids = if (page <= 2) listOf((page - 1) * 2 + 1, (page - 1) * 2 + 2) else emptyList()
                    val hs = mutableListOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        "Per-Page" to listOf("2"),
                    )
                    if (total != null) hs += "Total" to listOf(total)
                    respond("""{"stories":[${ids.joinToString(",") { storyJson(it) }}],"rels":[],"cv":1}""",
                        headers = headersOf(*hs.toTypedArray()))
                }
            }) {
                // The Ktor plugin sets this in production; the mock client has to opt in for 4xx/5xx to throw.
                expectSuccess = true
            },
        )

    /**
     * The first emission satisfying [predicate], or `null` if none arrives in time — the flow stays open for as long
     * as its scope does and never completes on its own, so every wait on it needs a bound.
     */
    private suspend fun <T> Flow<T>.awaitFirst(predicate: (T) -> Boolean): T? =
        withTimeoutOrNull(10.seconds) { first(predicate) }

    @Test
    fun `the flow emits the loaded stories`() = runBlocking {
        val snapshot = client().stories<Pg>(PagingConfig(pageSize = 2)).flow
            .asItemSnapshotListFlow()
            .awaitFirst { it.items.isNotEmpty() }

        // What the documented consumer pattern — asItemSnapshotListFlow().first() — sees.
        assertNotNull(snapshot)
        assertEquals(listOf("t1", "t2"), snapshot.items.map { it.content.title })
    }

    @Test
    fun `the flow emits a page whose response carries no Total header`() = runBlocking {
        val snapshot = client(total = null).stories<Pg>(PagingConfig(pageSize = 2)).flow
            .asItemSnapshotListFlow()
            .awaitFirst { it.items.isNotEmpty() }

        assertNotNull(snapshot)
        assertEquals(listOf("t1", "t2"), snapshot.items.map { it.content.title })
    }

    @Test
    fun `a client error reaches onLoadError as this client's own exception`() {
        assertWrapped(HttpStatusCode.NotFound)
    }

    @Test
    fun `a server error reaches onLoadError as this client's own exception`() {
        assertWrapped(HttpStatusCode.InternalServerError)
    }

    /**
     * Paging reports a failed load through [asItemSnapshotListFlow]'s callback rather than by throwing at the
     * collector, so the wait is on the callback and not on an emission — a failed load need not produce one.
     */
    private fun assertWrapped(status: HttpStatusCode): Unit = runBlocking {
        val reported = CompletableDeferred<Throwable>()
        val collecting = launch {
            client(status = status).stories<Pg>(PagingConfig(pageSize = 2)).flow
                .asItemSnapshotListFlow { states ->
                    listOf(states.refresh, states.append, states.prepend)
                        .filterIsInstance<LoadState.Error>()
                        .forEach { reported.complete(it.error) }
                }
                .collect { }
        }

        val error = withTimeoutOrNull(10.seconds) { reported.await() }
        // Joined, not just cancelled: an unawaited Paging job outlives the test and reports its
        // cancellation as a load error onto whichever test is collecting next.
        collecting.cancelAndJoin()

        assertNotNull(error, "no load error was reported for $status")
        assertIs<StoryblokClientException>(error)
    }

    @Test
    fun `a body that is not the shape this endpoint answers with reaches onLoadError as this client's own exception`() {
        // None of these reach the deserializer: they fail while the envelope is being traversed, so without the
        // wrapping they surface as whatever that traversal threw — a NullPointerException for a missing key, an
        // IllegalArgumentException for an element of the wrong type.
        for ((label, body) in listOf(
            "no stories key" to """{"cv":1}""",
            "not an object" to """<html>oops</html>""",
            "stories is not an array" to """{"stories":{},"cv":1}""",
            "a story without content" to """{"stories":[{"id":1}],"cv":1}""",
        )) {
            val error = awaitLoadError(body)
            assertNotNull(error, "no load error was reported for '$label'")
            assertIs<StoryblokClientException>(error, "'$label' surfaced as ${error::class.simpleName}")
        }
    }

    @Test
    fun `a story that does not fit its component keeps its deserialization failure`() {
        // The counterpart: this one does reach the deserializer, and a modelling error stays unwrapped so it is not
        // mistaken for a transient API failure worth retrying.
        // Pg.title has no default, and the client is lenient about types but cannot invent an absent field.
        val error = awaitLoadError(
            """{"stories":[${storyJson(1).replace(""","title":"t1"""", "")}],"cv":1}"""
        )
        assertNotNull(error)
        assertIs<SerializationException>(error)
    }

    /** The first load error reported for a 200 carrying [body], or `null` if none arrives in time. */
    private fun awaitLoadError(body: String): Throwable? = runBlocking {
        val reported = CompletableDeferred<Throwable>()
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Pg::class, Pg.serializer()) },
            jsonBuilder = { ignoreUnknownKeys = true; explicitNulls = false; coerceInputValues = true },
            http = HttpClient(MockEngine {
                respond(body, HttpStatusCode.OK, headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    "Total" to listOf("2"),
                    "Per-Page" to listOf("2"),
                ))
            }) { expectSuccess = true },
        )
        val collecting = launch {
            client.stories<Pg>(PagingConfig(pageSize = 2)).flow
                .asItemSnapshotListFlow { states ->
                    listOf(states.refresh, states.append, states.prepend)
                        .filterIsInstance<LoadState.Error>()
                        .forEach { reported.complete(it.error) }
                }
                .collect { }
        }
        val error = withTimeoutOrNull(10.seconds) { reported.await() }
        // Joined, not just cancelled: an unawaited Paging job outlives the test and reports its
        // cancellation as a load error onto whichever test is collecting next.
        collecting.cancelAndJoin()
        error
    }
}
