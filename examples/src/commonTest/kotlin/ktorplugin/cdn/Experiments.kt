package ktorplugin.cdn

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.Test

class Experiments {

    /**
     * Retrieve all running experiments from your Storyblok space using the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/experiments/retrieve-running-experiments
     */
    @Test
    fun `Retrieve Running Experiments`() = runTest {

        // implementation("com.storyblok:ktor-client-storyblok:0.5.1")
        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("experiments")
        
        println(response.body<JsonElement>())
    }

}