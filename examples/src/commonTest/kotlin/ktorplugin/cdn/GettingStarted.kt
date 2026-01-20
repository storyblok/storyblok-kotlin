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
class GettingStarted {

    /**
     * Discover how Storyblok's API authentication mechanism works through API access tokens.
     * https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/authentication
     */
    @Test
    fun Authentication() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "wANpEQEsMYGOwLxwXQ76Ggtt"
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
     * https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/cache-invalidation
     */
    @Test
    fun `Cache Invalidation`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "wANpEQEsMYGOwLxwXQ76Ggtt"
            }
        }
        
        val response = client.get("spaces/me")
        
        println(response.body<JsonElement>())
    }

    /**
     * Explore how Storyblok optimizes content delivery through its Content Delivery Network (CDN) and cache versioning mechanism. Learn about the cv parameter.
     * https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/cache-invalidation
     */
    @Test
    fun `Cache Invalidation 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "wANpEQEsMYGOwLxwXQ76Ggtt"
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