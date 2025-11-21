package ktorplugin.cdn

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test

class Stories {

	/**
     * Retrieve a single story by full slug, ID, or UUID using the Content Delivery API. Includes parameters for resolving links and relations.
     */
    @Test
    fun `Retrieve a Single Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "ask9soUkv02QqbZgmZdeDAtt")
		        }
		    }
		}
		
		val response = client.get("stories/posts/my-third-post")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve multiple stories from Storyblok using the Content Delivery API with filtering, pagination, sorting, and relation resolution options.
     */
    @Test
    fun `Retrieve Multiple Stories`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "ask9soUkv02QqbZgmZdeDAtt")
		        }
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
     * Example showing how to retrieve localized story versions using UUID and language parameters in the Content Delivery API.
     */
    @Test
    fun `Retrieving Localized Stories by UUID`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "krcV6QGxWORpYLUWt12xKQtt")
		        }
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
     */
    @Test
    fun `Retrieving Stories from a Folder`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "ask9soUkv02QqbZgmZdeDAtt")
		        }
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
     */
    @Test
    fun `Retrieving Stories in a Particular Language`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "krcV6QGxWORpYLUWt12xKQtt")
		        }
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
     */
    @Test
    fun `Retrieving Stories with Resolved Relations`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "krcV6QGxWORpYLUWt12xKQtt")
		        }
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
     */
    @Test
    fun `Sorting by Fields Associated with a Story Type`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "krcV6QGxWORpYLUWt12xKQtt")
		        }
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
     */
    @Test
    fun `Sorting by Story Object Property`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "krcV6QGxWORpYLUWt12xKQtt")
		        }
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