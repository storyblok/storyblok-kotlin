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

class GettingStarted {

	/**
     * Discover how Storyblok's API authentication mechanism works through API access tokens.
     */
    @Test
    fun Authentication() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "wANpEQEsMYGOwLxwXQ76Ggtt")
		        }
		    }
		}
		
		val response = client.get("stories") {
		    url {
		        parameters.append("version", "published")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Explore how Storyblok optimizes content delivery through its Content Delivery Network (CDN) and cache versioning mechanism. Learn about the cv parameter.
     */
    @Test
    fun `Cache Invalidation`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "wANpEQEsMYGOwLxwXQ76Ggtt")
		        }
		    }
		}
		
		val response = client.get("spaces/me")
		
		println(response.body<JsonElement>())
    }

	/**
     * Explore how Storyblok optimizes content delivery through its Content Delivery Network (CDN) and cache versioning mechanism. Learn about the cv parameter.
     */
    @Test
    fun `Cache Invalidation 2`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "wANpEQEsMYGOwLxwXQ76Ggtt")
		        }
		    }
		}
		
		val response = client.get("stories") {
		    url {
		        parameters.append("cv", "1541863983")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}