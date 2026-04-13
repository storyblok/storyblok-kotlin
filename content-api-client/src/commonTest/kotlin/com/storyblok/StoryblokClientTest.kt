package com.storyblok

import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api.Config.Version.Draft
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals

class StoryblokClientTest {

    @Serializable @SerialName("page")
    class Page(val title: String) : Component()
    @Serializable @SerialName("article")
    class Article(val author: Story<Component>) : Component()
    @Serializable @SerialName("featured")
    class FeaturedArticle(val article: Story<Article>) : Component()
    @Serializable @SerialName("popular")
    class PopularArticles(val articles: List<Story<Component>>) : Component()

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
}
