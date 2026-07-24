package com.storyblok

import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.StoryblokClientImpl
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
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class StoryblokClientTest {

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

        assertEquals(mapOf("article" to setOf("author")), client.relations)
    }

    @Test
    fun `a relation is added for a nested component inside a story property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, Article::class, Article.serializer())
            polymorphic(Component::class, FeaturedArticle::class, FeaturedArticle.serializer())
        })  as StoryblokClientImpl

        assertEquals(
            mapOf("featured" to setOf("article"), "article" to setOf("author")),
            client.relations
        )
    }

    @Test
    fun `a relation is added for a class with a story list property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, PopularArticles::class, PopularArticles.serializer())
        }) as StoryblokClientImpl

        assertEquals(mapOf("popular" to setOf("articles")), client.relations)
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
    }
}
