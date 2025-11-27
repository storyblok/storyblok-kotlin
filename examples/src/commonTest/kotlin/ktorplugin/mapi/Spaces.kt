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

class Spaces {

	/**
     * Trigger the backup task for your space. Make sure you've configured backups in your space options.
     * https://www.storyblok.com/docs/api/management/spaces/backup-a-space
     */
    @Test
    fun `Backup a Space`() = runTest {

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
		
		val response = client.post("spaces/12422/backups") {
		    contentType(ContentType.Application.Json)
		    setBody("""{}""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint creates a new space.
     * https://www.storyblok.com/docs/api/management/spaces/create-a-space
     */
    @Test
    fun `Create a Space`() = runTest {

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
		
		val response = client.post("spaces/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "space": {
		        "name": "Example Space"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a space by its numeric id.
     * https://www.storyblok.com/docs/api/management/spaces/delete-a-space
     */
    @Test
    fun `Delete a Space`() = runTest {

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
		
		val response = client.delete("spaces/12422")
		
		println(response.body<JsonElement>())
    }

	/**
     * Duplicate a space and all its content entries and components; Assets will not be duplicated and still will reference the original space.
     * https://www.storyblok.com/docs/api/management/spaces/duplicate-a-space
     */
    @Test
    fun `Duplicate a Space`() = runTest {

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
		
		val response = client.post("spaces/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "dup_id": 12422,
		      "space": {
		        "name": "Example Space"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single space object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/spaces/retrieve-a-single-space
     */
    @Test
    fun `Retrieve a Single Space`() = runTest {

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
		
		val response = client.get("spaces/606/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of space objects.
     * https://www.storyblok.com/docs/api/management/spaces/retrieve-multiple-spaces
     */
    @Test
    fun `Retrieve Multiple Spaces`() = runTest {

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
		
		val response = client.get("spaces/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a space using the numeric ID. You can only able to update the properties mentioned here.
     * https://www.storyblok.com/docs/api/management/spaces/update-a-space
     */
    @Test
    fun `Update a Space`() = runTest {

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
		
		val response = client.put("spaces/12422") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "space": {
		        "id": 12422,
		        "name": "Updated Example Space"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}