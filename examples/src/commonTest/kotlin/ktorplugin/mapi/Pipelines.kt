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

class Pipelines {

	/**
     * This endpoint creates a new branch.
     */
    @Test
    fun `Create a Branch`() = runTest {

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
		
		val response = client.post("spaces/606/branches/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "branch": {
		        "name": "A new branch",
		        "position": 2,
		        "source_id": 12332,
		        "url": "https://new_domain.com"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a branch using its numeric ID.
     */
    @Test
    fun `Delete a Branch`() = runTest {

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
		
		val response = client.delete("spaces/606/branches/14")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single branch object by providing a specific numeric id.
     */
    @Test
    fun `Retrieve a Single Branch`() = runTest {

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
		
		val response = client.get("spaces/606/branches/14")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of  branch objects
     */
    @Test
    fun `Retrieve Multiple Branches`() = runTest {

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
		
		val response = client.get("spaces/606/branches/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint updates a branch using its numeric ID.
     */
    @Test
    fun `Update a Branch`() = runTest {

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
		
		val response = client.put("spaces/606/branches/14") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "branch": {
		        "name": "Branche 123",
		        "position": 7,
		        "source_id": 12345,
		        "url": "https://new_url.com/"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}