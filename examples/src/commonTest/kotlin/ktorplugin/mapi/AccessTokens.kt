package ktorplugin.mapi

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

class AccessTokens {

	/**
     * Create an access token for a particular space.
     */
    @Test
    fun `Create an Access Token`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/606/api_keys/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "api_key": {
		        "access": "public",
		        "name": "My public Access token"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete an access token using its numeric ID.
     */
    @Test
    fun `Delete an Access Token`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("spaces/606/api_keys/2345")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of access token objects. The response of this endpoint is not paginated and you will retrieve all tokens.
     */
    @Test
    fun `Retrieve Multiple Access Tokens`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/api_keys/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update an access token with the numeric ID.
     */
    @Test
    fun `Update an Access Token`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/606/api_keys/123123") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "api_key": {
		        "access": "private",
		        "name": "My updated token"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}