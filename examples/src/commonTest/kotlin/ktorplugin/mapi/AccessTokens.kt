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
     * https://www.storyblok.com/docs/api/management/access-tokens/create-an-access-token
     */
    @Test
    fun `Create an Access Token`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/288868932106293/api_keys/") {
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
     * https://www.storyblok.com/docs/api/management/access-tokens/delete-an-access-token
     */
    @Test
    fun `Delete an Access Token`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("spaces/288868932106293/api_keys/2345")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of access token objects. The response of this endpoint is not paginated and you will retrieve all tokens.
     * https://www.storyblok.com/docs/api/management/access-tokens/retrieve-multiple-access-tokens
     */
    @Test
    fun `Retrieve Multiple Access Tokens`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/288868932106293/api_keys/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update an access token with the numeric ID.
     * https://www.storyblok.com/docs/api/management/access-tokens/update-an-access-token
     */
    @Test
    fun `Update an Access Token`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/288868932106293/api_keys/123123") {
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