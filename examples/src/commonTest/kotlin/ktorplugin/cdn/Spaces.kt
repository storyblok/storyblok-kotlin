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

class Spaces {

	/**
     * Retrieve information about the current Storyblok space including cache version, domain, and language configuration using the Content Delivery API.
     */
    @Test
    fun `Retrieve Current Space`() = runTest {

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
		
		val response = client.get("spaces/me")
		
		println(response.body<JsonElement>())
    }

}