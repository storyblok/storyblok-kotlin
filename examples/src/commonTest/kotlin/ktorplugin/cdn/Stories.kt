package ktorplugin.cdn

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test

@OptIn(ExperimentalSerializationApi::class)
class Stories {

    /**
     * Retrieve a single story by full slug, ID, or UUID using the Content Delivery API. Includes parameters for resolving links and relations.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-a-single-story
     */
    @Test
    fun `Retrieve a Single Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("stories/posts/my-third-post")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve multiple stories from Storyblok using the Content Delivery API with filtering, pagination, sorting, and relation resolution options.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("stories") {
            url {
                parameters.append("version", "published")
                parameters.append("starts_with", "articles")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Example showing how to retrieve a version of a story from a specific release by using the from_release query parameter.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-an-edited-version-of-a-story-from-a-release
     */
    @Test
    fun `Retrieving a Story from a Specific Release`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "krcV6QGxWORpYLUWt12xKQtt"
            }
        }
        
        val response = client.get("stories/home") {
            url {
                parameters.append("version", "draft")
                parameters.append("cv", "1765990908")
                parameters.append("from_release", "124105888551306")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Example showing how to retrieve localized story versions using UUID and language parameters in the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-localized-stories-by-uuid
     */
    @Test
    fun `Retrieving Localized Stories by UUID`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "krcV6QGxWORpYLUWt12xKQtt"
            }
        }
        
        val response = client.get("stories/660452d2-1a68-4493-b5b6-2f03b6fa722b") {
            url {
                parameters.append("find_by", "uuid")
                parameters.append("language", "de")
                parameters.append("version", "published")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Learn how to retrieve stories from specific folders using the starts_with parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-from-a-folder
     */
    @Test
    fun `Retrieving Stories from a Folder`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("stories") {
            url {
                parameters.append("starts_with", "articles/")
                parameters.append("version", "draft")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Example demonstrating how to retrieve translated story versions using the language parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-in-a-particular-language
     */
    @Test
    fun `Retrieving Stories in a Particular Language`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "krcV6QGxWORpYLUWt12xKQtt"
            }
        }
        
        val response = client.get("stories/articles/earths-symphony-navigating-wonders-challenges-blue-oasis") {
            url {
                parameters.append("language", "de")
                parameters.append("version", "published")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Learn how to resolve referenced stories using the resolve_relations parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-with-resolved-relations
     */
    @Test
    fun `Retrieving Stories with Resolved Relations`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "krcV6QGxWORpYLUWt12xKQtt"
            }
        }
        
        val response = client.get("stories") {
            url {
                parameters.append("resolve_relations", "article.categories,article.author")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Example showing how to sort stories by custom fields defined in your story type schema using the sort_by parameter.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/sorting-by-fields-associated-with-a-story-type
     */
    @Test
    fun `Sorting by Fields Associated with a Story Type`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "krcV6QGxWORpYLUWt12xKQtt"
            }
        }
        
        val response = client.get("stories/") {
            url {
                parameters.append("sort_by", "content.headline:asc")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Learn how to sort stories by default story properties like name, position, and publication dates using the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/sorting-by-story-object-property
     */
    @Test
    fun `Sorting by Story Object Property`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "krcV6QGxWORpYLUWt12xKQtt"
            }
        }
        
        val response = client.get("stories") {
            url {
                parameters.append("sort_by", "first_published_at:desc")
            }
        }
        
        println(response.body<JsonElement>())
    }

}