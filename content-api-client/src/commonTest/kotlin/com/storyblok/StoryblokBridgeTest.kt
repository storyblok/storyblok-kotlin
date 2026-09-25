package com.storyblok

import com.storyblok.cdn.StoryblokBridge
import com.storyblok.cdn.contentSerializer
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.storyblokJson
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.cdn.story
import io.ktor.client.HttpClient
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.util.reflect.serializer
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The Visual Editor's live updates, which [story] merges into what it fetched.
 *
 * No test platform has an editor, so these pass the client a [StoryblokBridge] of their own to
 * exercise everything downstream of one: the relaxed parsing the editor's payloads need, the
 * relations the editor sends already resolved, and the promise that nothing changes when there is no
 * editor at all.
 */
class StoryblokBridgeTest {

    @BeforeTest
    fun disableFileCache() { httpCacheStorage = { _ -> null } }

    @AfterTest
    fun restoreFileCache() { httpCacheStorage = ::fileCacheStorage }

    @Serializable @SerialName("page")
    class Page(val title: String) : Component()

    @Serializable @SerialName("article")
    class Article(val author: Story<Component>) : Component()

    @Serializable @SerialName("author")
    class Author(val name: String) : Component()

    /** What a Draft client decodes with, which the editor's payloads have to be parsed more leniently than. */
    private val json = storyblokJson(
        serializersModuleBuilder = {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, Article::class, Article.serializer())
            polymorphic(Component::class, Author::class, Author.serializer())
        },
        jsonBuilder = {
            explicitNulls = true
            coerceInputValues = false
            ignoreUnknownKeys = false
        },
    )

    /**
     * A bridge emitting [updates] and nothing else, standing in for the editor.
     *
     * It behaves the way the web target's bridge does, because the contract these tests exercise is
     * the interface's and not the browser's: it emits the fetched story first, takes only the
     * *content* of each update and keeps that story's envelope, drops an update it cannot decode,
     * and holds the newest update so that a subscription starting later — as one does every time a
     * fetch completes — is given it rather than waiting for the next keystroke.
     */
    private fun bridge(updates: (storyId: Long, resolveLevel: Int) -> Flow<JsonObject>) =
        object : StoryblokBridge {

            /** The newest update, kept for whoever subscribes next, as the editor's own flow keeps it. */
            private var held: JsonObject? = null

            override fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
                flow {
                    when (val edited = held) {
                        null -> emit(story)
                        else -> story.edited(edited, typeInfo)?.let { emit(it) }
                    }
                    updates(story.id, resolveLevel).collect { edited ->
                        held = edited
                        story.edited(edited, typeInfo)?.let { emit(it) }
                    }
                }

            override fun destroy() = Unit
        }

    /** [this] with the content of [edited] in place of its own, or `null` if that cannot be decoded. */
    private fun <T : Component> Story<T>.edited(edited: JsonObject, typeInfo: TypeInfo): Story<T>? =
        try {
            copy(content = json.decodeFromJsonElement(typeInfo.contentSerializer(), edited.getValue("content")))
        } catch (_: SerializationException) {
            null
        }

    /** A client serving [response], whose editor sends [updates]. */
    private fun client(response: String, vararg updates: String) =
        client(response) { _, _ -> flowOf(*updates.map { Json.parseToJsonElement(it).jsonObject }.toTypedArray()) }

    /** A client serving [response], previewed in an editor sending [updates]. */
    private fun client(
        response: String,
        updates: (storyId: Long, resolveLevel: Int) -> Flow<JsonObject>,
    ) = StoryblokClientImpl(
        json = json,
        http = HttpClient(MockEngine {
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }),
        bridge = bridge(updates),
    )

    @Test
    fun `the content serializer describes a story's content rather than its envelope`() {
        // The envelope's own descriptor is not a serializer and its first element is the story id,
        // so reaching for the content through it compiles and then fails on the first keystroke.
        val serializer = typeInfo<Story<Page>>().contentSerializer<Page>()

        assertEquals("page", serializer.descriptor.serialName)
    }

    @Test
    fun `a story completes after its fetch when there is no editor`() = runTest {
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Page::class, Page.serializer()) },
            jsonBuilder = {},
            http = HttpClient(MockEngine {
                respond(PAGE_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )

        // toList only returns once the flow completes, which is what callers key their loading
        // state off. An empty `liveStoryUpdates` must leave that untouched.
        val stories = client.story<Page>("home").toList()

        assertEquals(listOf("Home"), stories.map { it.content.title })
    }

    @Test
    fun `a payload that cannot be decoded is dropped and the next one still arrives`() = runTest {
        // The middle payload is a page mid-edit with its title cleared. On a fetch that is a
        // modelling error worth failing on; here it is one keystroke, and the edits after it have to
        // keep arriving.
        val client = client(
            PAGE_RESPONSE,
            editedPage("Home, edited"),
            edited("""{"component": "page", "_uid": "u1"}"""),
            editedPage("Home, edited twice"),
        )

        val titles = client.story<Page>("home").toList().map { it.content.title }

        assertEquals(listOf("Home", "Home, edited", "Home, edited twice"), titles)
    }

    @Test
    fun `a fetch that lands after an edit does not replace it`() = runTest {
        // story() fetches twice: once restricted to the HTTP cache, then once for real. The second
        // answer is the server's draft, which is older than anything the author has typed since —
        // and it arrives while they are typing.
        val edits = MutableSharedFlow<JsonObject>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val fresh = CompletableDeferred<Unit>()
        var request = 0

        val client = StoryblokClientImpl(
            json = json,
            http = HttpClient(MockEngine {
                if (++request > 1) fresh.await()
                respond(
                    if (request == 1) editedPageResponse("Cached") else editedPageResponse("Fresh"),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }),
            bridge = bridge { _, _ -> edits },
        )

        val seen = Channel<String>(Channel.UNLIMITED)
        val collecting = launch { client.story<Page>("home").collect { seen.send(it.content.title) } }

        assertEquals("Cached", seen.receive())

        edits.emit(Json.parseToJsonElement(editedPage("Typed")).jsonObject)
        assertEquals("Typed", seen.receive())

        // Only now does the fetch come back, carrying what the server had before that keystroke.
        fresh.complete(Unit)
        withContext(Dispatchers.Default) { delay(200) }

        val after = generateSequence { seen.tryReceive().getOrNull() }.toList()
        // Asserted before the interesting part, because a change that stops the second fetch
        // happening at all would satisfy every assertion below without fixing anything.
        assertEquals(2, request, "the fresh fetch never happened, so this proves nothing")
        assertEquals(
            emptyList(),
            after.filterNot { it == "Typed" },
            "the fetch put the story back to what the server held before the author typed",
        )

        collecting.cancel()
    }

    @Test
    fun `an edited story replaces the one that was fetched`() = runTest {
        val client = client(PAGE_RESPONSE, editedPage("Home, edited"), editedPage("Home, edited twice"))

        val titles = client.story<Page>("home").toList().map { it.content.title }

        assertEquals(listOf("Home", "Home, edited", "Home, edited twice"), titles)
    }

    @Test
    fun `an edited story keeps the fields the editor does not send`() = runTest {
        val client = client(PAGE_RESPONSE, editedPage("Home, edited"))

        val story = client.story<Page>("home").toList().last()

        assertEquals("home", story.fullSlug)
        assertEquals(1L, story.id)
    }

    @Test
    fun `an edited story keeps the envelope that was fetched rather than the editor's`() = runTest {
        // Only the content of an update is read. The editor reloads the preview when the story's own
        // slug or name changes, so nothing is lost by ignoring its envelope — and everything the
        // editor puts there that the Content Delivery API never returns stops being able to break
        // the decode.
        val client = client(
            PAGE_RESPONSE,
            edited("""{"component": "page", "_uid": "u1", "title": "Home, edited"}""", slug = "moved-in-editor", name = "Renamed"),
        )

        val story = client.story<Page>("home").toList().last()

        assertEquals("Home, edited", story.content.title)
        assertEquals("home", story.fullSlug)
        assertEquals("Story 1", story.name)
    }

    @Test
    fun `an edited story's relations arrive resolved by the editor`() = runTest {
        val client = client(ARTICLE_RESPONSE, editedArticle(EDITOR_RESOLVED_AUTHOR))

        val story = client.story<Article>("article").toList().last()

        assertEquals("Grace", (story.content.author.content as Author).name)
    }

    @Test
    fun `the resolve level the story was fetched with reaches the editor's updates`() = runTest {
        var seen: Int? = null
        val client = client(PAGE_RESPONSE) { _, level -> seen = level; flowOf() }

        client.story<Page>("home", resolveLevel = 3).toList()

        assertEquals(3, seen)
    }

    private companion object {

        const val STORY_UUID = "a51df0b5-6d29-4d0c-bd28-a54f47cf46bf"
        const val AUTHOR_UUID = "7bd51ad5-2f24-4a63-8b1a-cc2a2b0a9c11"

        /** A story JSON object with every field the Content Delivery API returns. */
        fun story(id: Int, uuid: String, slug: String, content: String) = """
            {
              "id": $id,
              "uuid": "$uuid",
              "name": "Story $id",
              "slug": "$slug",
              "full_slug": "$slug",
              "created_at": "2026-01-01T00:00:00.000Z",
              "published_at": null,
              "first_published_at": null,
              "updated_at": null,
              "sort_by_date": null,
              "position": 0,
              "tag_list": [],
              "is_startpage": false,
              "parent_id": null,
              "meta_data": null,
              "group_id": "941f4176-cbe4-4c15-9dd4-4384e136ac53",
              "lang": "default",
              "path": null,
              "alternates": [],
              "default_full_slug": null,
              "translated_slugs": null,
              "content": $content
            }
        """

        /** A response envelope carrying a page with [title], for tests needing two that differ. */
        fun editedPageResponse(title: String) = """
            { "story": ${story(1, STORY_UUID, "home", """{"component": "page", "_uid": "u1", "title": "$title"}""")} }
        """

        val PAGE_RESPONSE = """
            { "story": ${story(1, STORY_UUID, "home", """{"component": "page", "_uid": "u1", "title": "Home"}""")} }
        """

        val ARTICLE_RESPONSE = """
            {
              "story": ${story(1, STORY_UUID, "article", """{"component": "article", "_uid": "u1", "author": "$AUTHOR_UUID"}""")},
              "rels": [${story(2, AUTHOR_UUID, "ada", """{"component": "author", "_uid": "u2", "name": "Ada"}""")}]
            }
        """

        /**
         * An author story as the editor sends it: inlined in place of the uuid the Content Delivery
         * API returns.
         *
         * This is the only form a live relation takes. The editor is given the same
         * `resolve_relations` list the request is, and resolves every one of them — single fields,
         * list fields, fields on blocks nested in the body, and the relations of the stories it
         * inlines — so no uuid is ever left for the client to look up, and there is no `rels` on the
         * payload to look one up in.
         */
        val EDITOR_RESOLVED_AUTHOR = editorResolved(
            story(
                3,
                "c2b7fd7a-3adf-45f4-9e40-5b8ba18cbb15",
                "grace",
                """{"component": "author", "_uid": "u3", "name": "Grace"}""",
            )
        )

        /**
         * A story envelope as the editor inlines one, which is the Content Delivery API's shape plus
         * the fields only the editor has — `_stopResolving` being how it marks where it stopped
         * following a relation back to itself.
         *
         * A relation the editor resolves arrives inside the content, so this is an envelope the
         * client decodes even though the envelope around the whole story comes from the fetch.
         */
        fun editorResolved(story: String) = story.trim().removeSuffix("}") + """,
              "_stopResolving": true,
              "unpublished_changes": false,
              "breadcrumbs": [],
              "is_folder": false,
              "preview_token": { "token": "9f2a", "timestamp": "1758153600" }
            }
        """

        /**
         * A story as the Visual Editor pushes it: the Content Delivery API's shape, plus the fields
         * only the editor knows about, minus whichever optional ones it leaves out mid-edit.
         */
        fun edited(content: String, slug: String = "home", name: String = "Story 1") = """
            {
              "id": 1,
              "uuid": "$STORY_UUID",
              "name": "$name",
              "slug": "$slug",
              "full_slug": "$slug",
              "created_at": "2026-01-01T00:00:00.000Z",
              "published_at": null,
              "first_published_at": null,
              "updated_at": null,
              "sort_by_date": null,
              "position": 0,
              "tag_list": [],
              "is_startpage": false,
              "parent_id": null,
              "meta_data": null,
              "group_id": "941f4176-cbe4-4c15-9dd4-4384e136ac53",
              "lang": "default",
              "path": null,
              "alternates": [],
              "translated_slugs": null,
              "content": $content,
              "_editable": "<!--#storyblok#{\"name\": \"page\", \"id\": \"1\", \"uid\": \"u1\"}-->",
              "unpublished_changes": true,
              "is_folder": false,
              "pinned": false,
              "breadcrumbs": [],
              "preview_token": {"token": "abc", "timestamp": "1767225600"},
              "last_author": {"id": 1, "userid": "editor"}
            }
        """

        fun editedPage(title: String) = edited("""{"component": "page", "_uid": "u1", "title": "$title"}""")

        fun editedArticle(author: String) = edited("""{"component": "article", "_uid": "u1", "author": $author}""")
    }
}
