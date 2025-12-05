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

class Releases {

	/**
     * This endpoint allows you to create a new release.
     * https://www.storyblok.com/docs/api/management/releases/create-a-release
     */
    @Test
    fun `Create a Release`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/releases/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "release": {
		        "branches_to_deploy": [
		          123,
		          456
		        ],
		        "name": "Summer Special",
		        "release_at": "2025-01-01 01:01"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a release using its numeric id.
     * https://www.storyblok.com/docs/api/management/releases/delete-a-release
     */
    @Test
    fun `Delete a Release`() = runTest {

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
		
		val response = client.delete("spaces/288868932106293/releases/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single release object by providing a specific numeric ID.
     * https://www.storyblok.com/docs/api/management/releases/retrieve-a-single-release
     */
    @Test
    fun `Retrieve a Single Release`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/releases/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of releases.
     * https://www.storyblok.com/docs/api/management/releases/retrieve-multiple-releases
     */
    @Test
    fun `Retrieve Multiple Releases`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/releases/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows you to update a release using the numeric ID.
     * https://www.storyblok.com/docs/api/management/releases/update-a-release
     */
    @Test
    fun `Update a Release`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/releases/123") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "do_release": true,
		      "release": {
		        "branches_to_deploy": [
		          123,
		          456
		        ],
		        "name": "Summer Special",
		        "release_at": "2025-01-01 01:01"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}