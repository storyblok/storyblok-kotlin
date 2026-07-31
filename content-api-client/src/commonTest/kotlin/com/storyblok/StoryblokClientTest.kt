package com.storyblok

import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.StoryblokClientException
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import com.storyblok.cdn.story
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api.Config.Version.Draft
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class StoryblokClientTest {

    // FileStorage creates its directory as soon as a client is constructed, so without this the
    // tests would leave directories in the developer's real cache directory.
    @BeforeTest
    fun disableFileCache() { httpCacheStorage = { _ -> null } }

    @AfterTest
    fun restoreFileCache() { httpCacheStorage = ::fileCacheStorage }

    @Serializable @SerialName("page")
    class Page(val title: String) : Component()
    @Serializable @SerialName("article")
    class Article(val author: Story<Component>) : Component()
    @Serializable @SerialName("featured")
    class FeaturedArticle(val article: Story<Article>) : Component()
    @Serializable @SerialName("popular")
    class PopularArticles(val articles: List<Story<Component>>) : Component()
    @Serializable @SerialName("rich_page")
    class RichPage(val body: RichText.Document) : Component()
    @Serializable @SerialName("teaser")
    class Teaser(val headline: String) : Component()
    @Serializable @SerialName("related")
    class Related(val story: Story<Component>?) : Component()
    @Serializable @SerialName("related")
    class RelatedRaw(val story: String) : Component()
    @Serializable @SerialName("related")
    class RelatedRequired(val story: Story<Component>) : Component()

    @Test
    fun `a relation is not added for a class without a story property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
        }) as StoryblokClientImpl

        assertEquals(emptyMap(), client.relations)
    }

    @Test
    fun `a relation is added for a class with a story property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, Article::class, Article.serializer())
        }) as StoryblokClientImpl

        assertEquals(mapOf("article" to mapOf("author" to false)), client.relations)
    }

    @Test
    fun `a relation is added for a nested component inside a story property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, Article::class, Article.serializer())
            polymorphic(Component::class, FeaturedArticle::class, FeaturedArticle.serializer())
        })  as StoryblokClientImpl

        assertEquals(
            mapOf("featured" to mapOf("article" to false), "article" to mapOf("author" to false)),
            client.relations
        )
    }

    @Test
    fun `a relation is added for a class with a story list property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, PopularArticles::class, PopularArticles.serializer())
        }) as StoryblokClientImpl

        assertEquals(mapOf("popular" to mapOf("articles" to false)), client.relations)
    }

    @Test
    fun `a relation of a component embedded in rich text is resolved`() = runTest {
        var resolveRelations: String? = null
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = {
                polymorphic(Component::class, RichPage::class, RichPage.serializer())
                polymorphic(Component::class, PopularArticles::class, PopularArticles.serializer())
                polymorphic(Component::class, Teaser::class, Teaser.serializer())
            },
            jsonBuilder = {
                explicitNulls = false
                ignoreUnknownKeys = true
            },
            http = HttpClient(MockEngine { request ->
                resolveRelations = request.url.parameters["resolve_relations"]
                respond(EMBEDDED_RELATION_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )

        val story = client.story<RichPage>("home").first()

        assertEquals("popular.articles", resolveRelations)
        val block = story.content.body.flatten().filterIsInstance<RichText.Block>().single()
        val popular = block.body.single() as PopularArticles
        val article = popular.articles.single()
        assertEquals(Uuid.parse("9c5ef624-a238-4aba-af38-11b71183f6bc"), article.uuid)
        assertEquals("Hello", (article.content as Teaser).headline)
    }

    @Test
    fun `a circular relation resolves to null instead of recursing infinitely`() = runTest {
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = {
                polymorphic(Component::class, Related::class, Related.serializer())
            },
            jsonBuilder = {
                explicitNulls = false
                ignoreUnknownKeys = true
            },
            http = HttpClient(MockEngine {
                respond(CIRCULAR_RELATION_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )

        val story = client.story<Related>("first").first()

        val second = assertNotNull(story.content.story)
        val third = assertNotNull((second.content as Related).story)
        assertEquals(Uuid.parse("b599571b-df7e-4c85-97d9-0b0798d8b23f"), second.uuid)
        assertEquals(Uuid.parse("c2b7fd7a-3adf-45f4-9e40-5b8ba18cbb15"), third.uuid)
        assertNull((third.content as Related).story)
    }

    @Test
    fun `resolveLevel 2 resolves a circular relation one level deeper`() = runTest {
        var resolveLevel: String? = null
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = {
                polymorphic(Component::class, Related::class, Related.serializer())
            },
            jsonBuilder = {
                explicitNulls = false
                ignoreUnknownKeys = true
            },
            http = HttpClient(MockEngine { request ->
                resolveLevel = request.url.parameters["resolve_level"]
                respond(CIRCULAR_RELATION_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )

        val story = client.story<Related>("first", resolveLevel = 2).first()

        assertEquals("2", resolveLevel)
        val second = assertNotNull(story.content.story)
        val third = assertNotNull((second.content as Related).story)
        val fourth = assertNotNull((third.content as Related).story)
        val fifth = assertNotNull((fourth.content as Related).story)
        assertEquals(second.uuid, fourth.uuid)
        assertEquals(third.uuid, fifth.uuid)
        assertNull((fifth.content as Related).story)
    }

    @Test
    fun `an unresolved non-nullable relation fails with the uuid in the message`() = runTest {
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = {
                polymorphic(Component::class, RelatedRequired::class, RelatedRequired.serializer())
            },
            jsonBuilder = {
                explicitNulls = false
                ignoreUnknownKeys = true
            },
            http = HttpClient(MockEngine {
                respond(UNRESOLVED_RELATION_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )

        val exception = assertFailsWith<StoryblokClientException> { client.story<RelatedRequired>("first").first() }

        assertIs<SerializationException>(exception.cause)
        assertContains(assertNotNull(exception.message), "Unresolved story relation: d81538cf-5f75-4a5f-a8ab-a1e8fd276949")
    }

    @Test
    fun `a circular non-nullable relation fails with the uuid in the message`() = runTest {
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = {
                polymorphic(Component::class, RelatedRequired::class, RelatedRequired.serializer())
            },
            jsonBuilder = {
                explicitNulls = false
                ignoreUnknownKeys = true
            },
            http = HttpClient(MockEngine {
                respond(CIRCULAR_RELATION_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )

        val exception = assertFailsWith<StoryblokClientException> { client.story<RelatedRequired>("first").first() }

        assertIs<SerializationException>(exception.cause)
        assertContains(assertNotNull(exception.message), "Circular story relation: b599571b-df7e-4c85-97d9-0b0798d8b23f")
    }

    @Test
    fun `resolveLevel 0 disables relation resolution`() = runTest {
        var resolveRelations: String? = null
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = {
                polymorphic(Component::class, RelatedRaw::class, RelatedRaw.serializer())
                polymorphic(Component::class, Article::class, Article.serializer())
            },
            jsonBuilder = {
                explicitNulls = false
                ignoreUnknownKeys = true
            },
            http = HttpClient(MockEngine { request ->
                resolveRelations = request.url.parameters["resolve_relations"]
                respond(CIRCULAR_RELATION_RESPONSE, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )

        val story = client.story<RelatedRaw>("first", resolveLevel = 0).first()

        assertNull(resolveRelations)
        assertEquals("b599571b-df7e-4c85-97d9-0b0798d8b23f", story.content.story)
    }

    private companion object {

        /**
         * A `stories/home` response whose relation (`popular.articles`) sits inside a `blok` node of a rich text
         * field, so it is returned as a UUID in the content and as a full story in `rels`.
         */
        val EMBEDDED_RELATION_RESPONSE = """
            {
              "story": {
                "id": 1,
                "uuid": "5edd824a-966d-4585-9b33-e28f8dab9bc3",
                "name": "Home",
                "slug": "home",
                "full_slug": "home",
                "created_at": "2026-01-01T00:00:00.000Z",
                "position": 0,
                "tag_list": [],
                "is_startpage": false,
                "group_id": "941f4176-cbe4-4c15-9dd4-4384e136ac53",
                "lang": "default",
                "alternates": [],
                "content": {
                  "component": "rich_page",
                  "_uid": "0a1e0a90-0000-0000-0000-000000000001",
                  "body": {
                    "type": "doc",
                    "content": [
                      {
                        "type": "paragraph",
                        "content": [{"type": "text", "text": "Intro"}]
                      },
                      {
                        "type": "blok",
                        "attrs": {
                          "id": "0a1e0a90-0000-0000-0000-000000000002",
                          "body": [
                            {
                              "component": "popular",
                              "_uid": "0a1e0a90-0000-0000-0000-000000000003",
                              "articles": ["9c5ef624-a238-4aba-af38-11b71183f6bc"]
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
              },
              "rels": [
                {
                  "id": 2,
                  "uuid": "9c5ef624-a238-4aba-af38-11b71183f6bc",
                  "name": "Teaser",
                  "slug": "teaser",
                  "full_slug": "articles/teaser",
                  "created_at": "2026-01-01T00:00:00.000Z",
                  "position": 0,
                  "tag_list": [],
                  "is_startpage": false,
                  "group_id": "941f4176-cbe4-4c15-9dd4-4384e136ac53",
                  "lang": "default",
                  "alternates": [],
                  "content": {
                    "component": "teaser",
                    "_uid": "0a1e0a90-0000-0000-0000-000000000004",
                    "headline": "Hello"
                  }
                }
              ]
            }
        """

        /** Builds a minimal story JSON object whose `related` content references [relation]. */
        fun relatedStory(id: Int, uuid: String, slug: String, relation: String) = """
            {
              "id": $id,
              "uuid": "$uuid",
              "name": "Related $id",
              "slug": "$slug",
              "full_slug": "$slug",
              "created_at": "2026-01-01T00:00:00.000Z",
              "position": 0,
              "tag_list": [],
              "is_startpage": false,
              "group_id": "941f4176-cbe4-4c15-9dd4-4384e136ac53",
              "lang": "default",
              "alternates": [],
              "content": {
                "component": "related",
                "_uid": "0a1e0a90-0000-0000-0000-00000000000$id",
                "story": "$relation"
              }
            }
        """

        /**
         * A `stories/first` response whose relation (`related.story`) references a story that is not in `rels`.
         */
        val UNRESOLVED_RELATION_RESPONSE = """
            {
              "story": ${relatedStory(1, "a51df0b5-6d29-4d0c-bd28-a54f47cf46bf", "first", "d81538cf-5f75-4a5f-a8ab-a1e8fd276949")},
              "rels": []
            }
        """

        /**
         * A `stories/first` response whose two rels reference each other (`related.story`): the second story points to
         * the third and the third back to the second, so resolution must cut the cycle instead of recursing forever.
         */
        val CIRCULAR_RELATION_RESPONSE = """
            {
              "story": ${relatedStory(1, "a51df0b5-6d29-4d0c-bd28-a54f47cf46bf", "first", "b599571b-df7e-4c85-97d9-0b0798d8b23f")},
              "rels": [
                ${relatedStory(2, "b599571b-df7e-4c85-97d9-0b0798d8b23f", "second", "c2b7fd7a-3adf-45f4-9e40-5b8ba18cbb15")},
                ${relatedStory(3, "c2b7fd7a-3adf-45f4-9e40-5b8ba18cbb15", "third", "b599571b-df7e-4c85-97d9-0b0798d8b23f")}
              ]
            }
        """
    }
}
             