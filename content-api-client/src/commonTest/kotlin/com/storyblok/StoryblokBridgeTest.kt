package com.storyblok

import com.storyblok.cdn.StoryblokBridge
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.storyblokJson
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.cdn.story
import io.ktor.client.HttpClient
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.serializer
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
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
     * A bridge reporting [updates] and nothing else, standing in for the editor.
     *
     * It decodes them the way the web target's bridge does, with the client's own [json], so that
     * what the editor sends is still parsed by the code path that parses it in the browser.
     */
    private fun bridge(updates: (storyId: Long, resolveLevel: Int) -> Flow<JsonObject>) =
        object : StoryblokBridge {
            override fun <T : Component> story(storyId: Long, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
                updates(storyId, resolveLevel).map {
                    @Suppress("UNCHECKED_CAST")
                    @OptIn(io.ktor.utils.io.InternalAPI::class)
                    json.decodeFromJsonElement(typeInfo.serializer() as KSerializer<Story<T>>, it)
                }

            override fun destroy() = Unit
        }

    /** A client serving [response], whose editor reports [updates]. */
    private fun client(response: String, vararg updates: String) =
        client(response) { _, _ -> flowOf(*updates.map { Json.parseToJsonElement(it).jsonObject }.toTypedArray()) }

    /** A client serving [response], previewed in an editor reporting [updates]. */
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
        val EDITOR_RESOLVED_AUTHOR = story(
            3,
            "c2b7fd7a-3adf-45f4-9e40-5b8ba18cbb15",
            "grace",
            """{"component": "author", "_uid": "u3", "name": "Grace"}""",
        )

        /**
         * A story as the Visual Editor pushes it: the Content Delivery API's shape, plus the fields
         * only the editor knows about, minus whichever optional ones it leaves out mid-edit.
         */
        fun edited(content: String) = """
            {
              "id": 1,
              "uuid": "$STORY_UUID",
              "name": "Story 1",
              "slug": "home",
              "full_slug": "home",
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
