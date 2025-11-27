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

class Assets {

	/**
     * Retrieves a signed URL to access a private asset.
     * https://www.storyblok.com/docs/api/content-delivery/v2/assets/get-signed-url
     */
    @Test
    fun `Get Signed URL`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "pL43PrKPdU9kW6R1DKoFrgtt")
		        }
		    }
		}
		
		val response = client.get("assets/me") {
		    url {
		        parameters.append("filename", "https://a.storyblok.com/f/184738/3500x2000/c817583540/mars.jpg")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}