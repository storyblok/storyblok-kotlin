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
class Links {

    /**
     * Retrieve a concise representation of stories and folders using the links endpoint in the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/links/retrieve-multiple-links
     */
    @Test
    fun `Retrieve Multiple Links`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "krcV6QGxWORpYLUWt12xKQtt"
            }
        }
        
        val response = client.get("links") {
            url {
                parameters.append("version", "published")
                parameters.append("starts_with", "articles")
            }
        }
        
        println(response.body<JsonElement>())
    }

}