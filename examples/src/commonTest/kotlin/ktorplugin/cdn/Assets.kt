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
class Assets {

    /**
     * Retrieves a signed URL to access a private asset.
     * https://www.storyblok.com/docs/api/content-delivery/v2/assets/get-signed-url
     */
    @Test
    fun `Get Signed URL`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "cNGPp8cvuCfoAZB3g3eHrAtt"
            }
        }
        
        val response = client.get("assets/me") {
            url {
                parameters.append("filename", "https://a.storyblok.com/f/44203/x/5231aa9c8a/favicon.ico")
            }
        }
        
        println(response.body<JsonElement>())
    }

}