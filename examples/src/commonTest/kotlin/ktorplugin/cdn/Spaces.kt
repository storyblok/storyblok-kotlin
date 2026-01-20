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
class Spaces {

    /**
     * Retrieve information about the current Storyblok space including cache version, domain, and language configuration using the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/spaces/retrieve-current-space
     */
    @Test
    fun `Retrieve Current Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("spaces/me")
        
        println(response.body<JsonElement>())
    }

}