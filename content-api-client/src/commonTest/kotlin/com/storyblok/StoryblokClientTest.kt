package com.storyblok

import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api
import com.storyblok.ktor.Api.CDN
import com.storyblok.ktor.Api.Config.Management.AccessToken
import com.storyblok.ktor.Api.Config.Management.AccessToken.OAuth
import com.storyblok.ktor.Api.Config.Version.Draft
import com.storyblok.ktor.Api.MAPI
import com.storyblok.ktor.Storyblok
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
        })
        assertEquals(emptyMap(), client.relations)
    }

    @Test
    fun `a relation is added for a class with a story property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, Article::class, Article.serializer())
        })
        assertEquals(mapOf("article" to setOf("author")), client.relations)
    }

//    @Test
//    fun `a relation is added for a nested component inside a story property`() = runTest {
//        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
//            polymorphic(Component::class, Page::class, Page.serializer())
//            polymorphic(Component::class, FeaturedArticle::class, FeaturedArticle.serializer())
//        })
//        assertEquals(
//            mapOf("featured" to setOf("article"), "article" to setOf("author")),
//            client.relations
//        )
//    }

    @Test
    fun `a relation is added for a class with a story list property`() = runTest {
        val client = StoryblokClient("mock-api-key", Draft, serializersModule = SerializersModule {
            polymorphic(Component::class, Page::class, Page.serializer())
            polymorphic(Component::class, PopularArticles::class, PopularArticles.serializer())
        })
        assertEquals(mapOf("popular" to setOf("articles")), client.relations)
    }
}
